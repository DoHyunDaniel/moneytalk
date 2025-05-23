package com.example.moneytalk.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.moneytalk.config.S3Uploader;
import com.example.moneytalk.domain.Product;
import com.example.moneytalk.domain.ProductImage;
import com.example.moneytalk.exception.GlobalException;
import com.example.moneytalk.repository.ProductImageRepository;
import com.example.moneytalk.repository.ProductRepository;
import com.example.moneytalk.type.ErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * ProductImageService
 * 상품에 등록된 이미지 정보를 처리하는 서비스입니다.
 *
 * [기능 설명]
 * - 특정 상품 ID를 기준으로 이미지 URL 리스트를 조회합니다.
 * - 상품 존재 여부를 먼저 검증한 후, 이미지 정보를 반환합니다.
 *
 * @author Daniel
 * @since 2025.04.15
 */
@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final S3Uploader s3Uploader;
    
    /**
     * 주어진 상품 ID에 해당하는 이미지 URL 목록을 반환합니다.
     *
     * @param productId 이미지 정보를 조회할 상품 ID
     * @return 이미지 URL 리스트
     * @throws GlobalException 상품이 존재하지 않을 경우 {@link ErrorCode#PRODUCT_NOT_FOUND}
     */
    public List<String> getImageUrlsByProductId(Long productId) {
        // 상품 존재 여부 확인
        if (!productRepository.existsById(productId)) {
            throw new GlobalException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        return productImageRepository.findByProductId(productId)
                .stream()
                .map(image -> image.getImageUrl())
                .toList();
    }
    
    
    /**
     * 상품 이미지 리스트를 업로드하고, 대표 이미지(썸네일)를 지정합니다.
     *
     * @param productId 업로드할 상품의 ID
     * @param images 업로드할 이미지 파일 리스트
     * @param thumbnailIndex 대표 이미지로 설정할 이미지 인덱스 (null일 경우 첫 번째 이미지로 설정)
     * @throws GlobalException 상품이 존재하지 않는 경우 {@link ErrorCode#PRODUCT_NOT_FOUND}
     */
    @Transactional
    public void uploadProductImages(Long productId, List<MultipartFile> images, Integer thumbnailIndex) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new GlobalException(ErrorCode.PRODUCT_NOT_FOUND));

        for (int i = 0; i < images.size(); i++) {
            MultipartFile file = images.get(i);
            String imageUrl = s3Uploader.uploadFile(file, "products");

            boolean isThumbnail = (thumbnailIndex != null && i == thumbnailIndex);

            ProductImage image = ProductImage.builder()
                .product(product)
                .imageUrl(imageUrl)
                .isThumbnail(isThumbnail)
                .build();

            productImageRepository.save(image);
        }

        // 만약 사용자가 대표 이미지를 지정하지 않은 경우 → 첫 번째 이미지를 대표로 설정
        if (thumbnailIndex == null && !images.isEmpty()) {
            List<ProductImage> savedImages = productImageRepository.findByProduct(product);
            if (!savedImages.isEmpty()) {
                savedImages.get(0).setThumbnail(true);
            }
        }
    }

}
