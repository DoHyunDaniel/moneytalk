import {
    createContext,
    useContext,
    useEffect,
    useState,
    ReactNode,
  } from "react";
  import {
    login as loginAPI,
    logout as logoutAPI,
    getMyInfo,
  } from "../api/auth";
  
  interface User {
    id: number;
    email: string;
    nickname: string;
  }
  
  interface AuthContextType {
    user: User | null;
    login: (email: string, password: string) => Promise<void>;
    logout: () => Promise<void>;
  }
  
  const AuthContext = createContext<AuthContextType | undefined>(undefined);
  
  export const AuthProvider = ({ children }: { children: ReactNode }) => {
    const [user, setUser] = useState<User | null>(null);
  
    useEffect(() => {
      const fetchUser = async () => {
        try {
          const data = await getMyInfo();
          setUser(data);
        } catch (e) {
          setUser(null);
        }
      };
      fetchUser();
    }, []);
  
    const login = async (email: string, password: string) => {
      await loginAPI({ email, password });
      const data = await getMyInfo(); // 로그인 후 사용자 정보 가져오기
      setUser(data);
    };
  
    const logout = async () => {
      await logoutAPI();
      setUser(null);
    };
  
    return (
      <AuthContext.Provider value={{ user, login, logout }}>
        {children}
      </AuthContext.Provider>
    );
  };
  
  export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
      throw new Error("useAuth는 AuthProvider 내부에서만 사용해야 합니다.");
    }
    return context;
  };
  