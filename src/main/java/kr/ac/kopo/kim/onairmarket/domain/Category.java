package kr.ac.kopo.kim.onairmarket.domain;

import java.util.List;
import java.util.Optional;

/** 기존 온에어마켓(app/ui-categories.ts) 둘러보기 카테고리와 같은 id·이름 */
public record Category(String id, String label) {
    public static final List<Category> ALL = List.of(
            new Category("tcg", "트레이딩 카드 게임"), new Category("pokemon", "포켓몬 카드"),
            new Category("women", "여성 패션"), new Category("entertainment", "엔터테인먼트 카드"),
            new Category("books", "책·영화"), new Category("sports-cards", "스포츠 카드"),
            new Category("toys", "장난감·취미"), new Category("electronics", "전자기기"),
            new Category("coins", "주화·화폐"), new Category("estate", "보물찾기·창고 세일"),
            new Category("memorabilia", "스포츠 수집품"), new Category("men", "남성 패션"),
            new Category("sneakers", "스니커즈·신발"), new Category("bags", "가방·액세서리"),
            new Category("beauty", "뷰티"), new Category("jewelry", "주얼리"),
            new Category("music", "음악"), new Category("games", "비디오 게임"),
            new Category("antiques", "앤티크·빈티지 소품"), new Category("home", "홈·가드닝"),
            new Category("crystals", "원석·크리스털"), new Category("kids", "유아·키즈"),
            new Category("handmade", "아트·핸드메이드"), new Category("comics", "코믹스"),
            new Category("outdoors", "아웃도어"), new Category("wholesale", "도매·특가"),
            new Category("anime", "애니·만화"), new Category("food", "푸드·음료"),
            new Category("sporting", "스포츠 용품"), new Category("pets", "반려동물"),
            new Category("japan", "일본 셀렉트"), new Category("halloween", "할로윈"),
            new Category("vintage", "빈티지"), new Category("watches", "시계"),
            new Category("everything", "그 밖의 취향"));

    public static Optional<Category> find(String id) {
        return ALL.stream().filter(c -> c.id().equals(id)).findFirst();
    }
}
