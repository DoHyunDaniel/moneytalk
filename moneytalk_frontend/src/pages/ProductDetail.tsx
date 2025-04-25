import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { getProductById, createChatRoom } from "../api/productApi";
import { useAuth } from "../context/AuthContext";

interface Product {
  id: number;
  sellerId: number;
  title: string;
  description: string;
  price: number;
  category: string;
  location: string;
  status: string;
  createdAt: string;
  sellerNickname: string;
  images: string[];
}

const ProductDetail = () => {
  const { productId } = useParams<{ productId: string }>();
  const [product, setProduct] = useState<Product | null>(null);
  const navigate = useNavigate();
  const { user } = useAuth();

  useEffect(() => {
    if (!productId) return;
    getProductById(Number(productId))
      .then(setProduct)
      .catch((err) => console.error("상품 조회 실패", err));
  }, [productId]);

  const handleChat = async () => {
    if (!product || !user) return;
    try {
      const res = await createChatRoom(product.id);
      navigate(`/chat/${res.data.chatRoomId}`);
    } catch (err) {
      console.error("채팅방 생성 실패", err);
      alert("채팅방 생성에 실패했습니다.");
    }
  };

  if (!product) return <div>로딩 중...</div>;

  return (
    <div>
      {product.images.length > 0 && (
        <img src={product.images[0]} width={200} alt="상품 이미지" />
      )}
      <h1>상품 상세</h1>
      <h2>{product.title}</h2>
      <p>{product.description}</p>
      <p>💰 {product.price}원</p>
      <p>
        📍 {product.category} · {product.location}
      </p>
      <p>👤 판매자: {product.sellerNickname}</p>

      <button onClick={handleChat}>💬 채팅하기</button>
    </div>
  );
};

export default ProductDetail;
