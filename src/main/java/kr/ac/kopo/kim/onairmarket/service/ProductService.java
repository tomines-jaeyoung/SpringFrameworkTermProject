package kr.ac.kopo.kim.onairmarket.service;

import kr.ac.kopo.kim.onairmarket.domain.AuctionMedia;
import kr.ac.kopo.kim.onairmarket.domain.Category;
import kr.ac.kopo.kim.onairmarket.domain.Member;
import kr.ac.kopo.kim.onairmarket.domain.Product;
import kr.ac.kopo.kim.onairmarket.dto.ProductForm;
import kr.ac.kopo.kim.onairmarket.exception.ServiceException;
import kr.ac.kopo.kim.onairmarket.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/** 즉시 구매(중고 거래형): 상품 등록, 목록, 상세, 구매 */
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final MediaService mediaService;

    /** 등록: 입력값 검사 → 사진(필수) 저장 → 상품 저장. 상품 저장이 실패하면 저장한 사진을 정리한다. */
    public Product create(ProductForm form, MultipartFile image, Member owner) {
        long now = System.currentTimeMillis();
        Category category = Category.find(form.getCategoryId())
                .orElseThrow(() -> new ServiceException("카테고리를 선택해 주세요."));
        if (image == null || image.isEmpty()) throw new ServiceException("상품 사진을 올려 주세요.");

        AuctionMedia img = null;
        try {
            img = mediaService.store(image, "image", owner);
            Product p = new Product();
            p.setId(UUID.randomUUID().toString());
            p.setOwner(owner);
            p.setSeller(owner.getName());
            p.setTitle(form.getTitle().trim());
            p.setDescription(form.getDescription().trim());
            p.setCategoryId(category.id());
            p.setCategory(category.label());
            p.setImage("/api/media/" + img.getId());
            p.setPrice(form.getPrice());
            p.setCreated(now);
            p.setUpdated(now);
            return productRepository.save(p);
        } catch (RuntimeException e) {
            mediaService.discard(img);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<Product> list(String categoryId, String keyword, boolean sellingOnly, int page) {
        Sort order = Sort.by("created").descending().and(Sort.by("id").descending());
        return productRepository.search(categoryId, keyword, sellingOnly, PageRequest.of(Math.max(page, 0), 12, order));
    }

    /** 홈: 판매 중인 최근 상품 4개 */
    @Transactional(readOnly = true)
    public List<Product> latestSelling() {
        return productRepository.findTop4ByStatusOrderByCreatedDesc(Product.SELLING);
    }

    @Transactional(readOnly = true)
    public Product get(String id) {
        Product p = productRepository.findById(id).orElseThrow(() -> new ServiceException("상품을 찾을 수 없습니다."));
        p.getOwner().getId();   // 지연 로딩 초기화 (본인 상품 비교용)
        return p;
    }

    /** 구매: 판매 중 · 본인 상품 아님을 확인하고, 조건부 갱신으로 먼저 누른 한 명만 성공시킨다. */
    @Transactional
    public void buy(String id, Member buyer) {
        Product p = productRepository.findById(id).orElseThrow(() -> new ServiceException("상품을 찾을 수 없습니다."));
        if (p.getOwner().getId().equals(buyer.getId())) throw new ServiceException("본인 상품은 구매할 수 없습니다.");
        if (p.isSold()) throw new ServiceException("이미 판매된 상품입니다.");
        int changed = productRepository.markSold(id, buyer, buyer.getName(), System.currentTimeMillis());
        if (changed != 1) throw new ServiceException("이미 판매된 상품입니다.");
    }

    @Transactional(readOnly = true)
    public List<Product> mySales(Member m) {
        return productRepository.findByOwnerIdOrderByCreatedDesc(m.getId());
    }

    @Transactional(readOnly = true)
    public List<Product> myPurchases(Member m) {
        return productRepository.findByBuyerIdOrderBySoldAtDesc(m.getId());
    }
}
