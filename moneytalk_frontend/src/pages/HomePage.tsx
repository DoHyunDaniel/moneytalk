// src/pages/HomePage.tsx
import { useEffect, useState } from "react";
import { getMyInfo } from "../api/auth";

const HomePage = () => {
  const [user, setUser] = useState<any>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const data = await getMyInfo();
        setUser(data);
      } catch (err) {
        console.error("로그인 안 되어 있음");
      }
    };
    fetchData();
  }, []);

  return (
    <div>
      {user ? (
        <h2>환영합니다, {user.nickname}님!</h2>
      ) : (
        <h2>로그인되어 있지 않습니다.</h2>
      )}
    </div>
  );
};

export default HomePage;
