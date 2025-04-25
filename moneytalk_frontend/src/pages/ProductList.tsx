import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getAllProducts } from "../api/productApi";

interface Product {
  id: number;
  title: string;
  price: number;
  category: string;
  location: string;
  thumbnailUrl: string;
}

const ProductList = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const navigate = useNavigate();

  useEffect(() => {
    getAllProducts().then(setProducts);
  }, []);

  return (
    <div>
      <h2>📦 전체 상품 목록</h2>
      <ul>
        {products.map((product) => (
          <li
            key={product.id}
            style={{ cursor: "pointer", margin: "12px 0" }}
            onClick={() => navigate(`/products/${product.id}`)}
          >
            <img src={product.thumbnailUrl} width={100} alt="thumb" />
            <div>{product.title} - {product.price}원</div>
            <div>{product.category} · {product.location}</div>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default ProductList;
