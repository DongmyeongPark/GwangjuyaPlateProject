package com.smart.project.web.home.act;

import com.smart.project.proc.Test;
import com.smart.project.web.home.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;
@SessionAttributes("pageNum")
@Slf4j
@RestController
@RequiredArgsConstructor
public class HomeDataAct {
	final private Test test;

	@PostMapping("/data/mango2All")
	public Map<String, Object> getMango2DataAll(@RequestBody Map param){
		Map<String, Object> result = new HashMap<>();
		List<MangoVO> data = test.selectMango2All();
		/*List<mango2VO> data=null;*/
		log.error("select 결과 list : {}",data);
		result.put("food",data);

		return result;
	}
	// 검색 입력 or 음식 메뉴 클릭 시 음식점 리스트 띄우기
	@PostMapping("/data/searchAll")
	public Map<String, Object> getSearchAll(Model model ,@RequestBody Map param, HttpSession session, Criteria cri){
		Map<String, Object> result = new HashMap<>();
		String selectAlign ="";

		String search = "";
		int pageNum = 1;
		if(param.get("search") != null)
			search = String.valueOf(param.get("search"));
		if(param.get("pageNum") != null )
			pageNum = Integer.parseInt(String.valueOf(param.get("pageNum")));
		if(param.get("selectAlign") != null)
			selectAlign = String.valueOf(param.get("selectAlign"));
		model.addAttribute("pageNum",pageNum);
		cri.setSearch(search); // 검색 창에 입력한 것
		cri.setPage(pageNum); // 페이지 번호  1번누르면 1번 set
		for(MangoVO vo :test.searchAll(cri))
		{
			String placename = vo.getName();
			test.reviewAvg(placename);
			test.rvShow(placename);
		}
		//데이터 정렬
		if(selectAlign.equals("리뷰 많은순")){
			List<MangoVO> data = test.searchAllReviewCount(cri);
			result.put("food",data);
		} else if (selectAlign.equals("조회순"))
		{
			List<MangoVO> data = test.searchAllShowCount(cri);
			result.put("food",data);
		} else{

			List<MangoVO> data = test.searchAll(cri);

			result.put("food",data);
		}


		//페이징처리
		int totalCount = test.totalCount(cri);
		if(!(totalCount==0))
		{
			PageMaker pageMaker = new PageMaker();
			pageMaker.setCri(cri);
			pageMaker.setTotalCount(totalCount);
			result.put("page",pageMaker);
		}
		else {
			result.put("page",null);
		}

		return result;
	}


	@PostMapping("/data/mango2")
	public List<MangoVO>getMango2Data(@RequestBody Map param, Criteria cri, Model model){
	 String mainmenu = String.valueOf(param.get("menu"));
	 String search = String.valueOf(param.get("menu"));
	 cri.setSearch(search);

	 log.error("검색창에 입력한 것 : {}",mainmenu);
	 List<MangoVO> data = test.selectMango2(mainmenu);
		/*List<mango2VO> data=null;*/
	log.error("select 결과 list : {}",data);
		return data;
	}

	@PostMapping("/data/map")
	public List<MangoVO>getMapData(@RequestBody Map param){
		String name = String.valueOf(param.get("name"));
		log.error("클릭한 맛집  : {}",name);
		test.viewCount(name);
		List<MangoVO> data = test.selectName(name);
		return data;
	}
	//위시리스트 값 가져와서 저장하기
	@RequestMapping("/wishStore")
	public WishListVO getData(@ModelAttribute WishListVO vo, HttpServletRequest request){
		String useremail = (String) request.getSession().getAttribute("email");
		vo.setUseremail(useremail);
		WishListVO data = vo;
			test.insertWish(vo);
		return data;
	}
	//위시리스트에 DB저장된 값 출력
	@RequestMapping("/data/wishSelect")
	public List<WishListVO> wishSelect(HttpServletRequest request){
		String useremail = (String)request.getSession().getAttribute("email");
		List<WishListVO> data = test.selectWish(useremail);
		return data;
	}
	//위시리스트에 선택한 리스트 삭제
	@RequestMapping("data/wishDelete")
	public WishListVO wishDelete(@RequestBody Map param, HttpServletRequest request){
		WishListVO vo = new WishListVO();
		String useremail = (String)request.getSession().getAttribute("email");
		String placename = (String)param.get("placeName");
		log.error("가져온 이메일 => {}",useremail);
		log.error("가져온 장소 => {}",placename);
		vo.setUseremail(useremail);
		vo.setPlacename(placename);
		test.wishDelete(vo);
		WishListVO data = vo;
		log.error("지운 data => {}",data);

		return data;
	}
	//위시리스트 확인 후 있으면 리턴받아 별표 색 유지
	@RequestMapping("data/haveWish")
	public WishListVO haveWish(@RequestBody Map param,HttpServletRequest request){
		WishListVO vo = new WishListVO();
		String useremail = (String)request.getSession().getAttribute("email");
		String placename = (String)param.get("placeName");
		vo.setUseremail(useremail);
		vo.setPlacename(placename);
		WishListVO data = test.haveWish(vo);
		return data;
	}

	//해당 이메일에 로그인되어있을 때 리뷰 삭제
	@RequestMapping("data/deleteReply")
	public void deleteReply(@ModelAttribute ReviewDTO dto) {
		String email =  dto.getEmail();
		String title = dto.getTitle();
		dto.setEmail(email);
		log.error("지울 것? => {}",dto.getEmail());
		ReviewDTO data = dto;

		test.reviewCount(title, -1);
		test.deleteReply(email);
	}
	// 해당 이메일로 로그인되었을때 리뷰 변경
	@RequestMapping("updateReview")
	public void updateReview(@ModelAttribute ReviewDTO reviewDTO){
		log.error("{}==>asdfasdfa",reviewDTO);

		test.updateReview(reviewDTO);

	}

	@PostMapping("/idCheck")
	public int checkDuplicateId(@RequestBody Map param){
		String id = String.valueOf(param.get("userId"));
		int idCount = test.idCount(id);

		return idCount;
	}
	@PostMapping("/saveReview")
	public void saveReview(ReviewDTO reviewDTO) {
		log.error("{}===>",reviewDTO);
		String id = reviewDTO.getEmail();
		String title = reviewDTO.getTitle();
		test.reviewCount(title, 1);
		log.error("{}===>",id+"id");
		test.saveReview(reviewDTO);

	}
	@RequestMapping("/getReview")
	public ReviewDTO getReview(String reviewId) {
		log.error("{}===>",reviewId+"reviewId");
		return test.getReview(reviewId);
	}
	@RequestMapping("/getReviewsByKeySet")
	public List<ReviewDTO> getReviewsByKeySet(String reviewUpdateDate, String reviewId) {
		log.error("{}===>",reviewUpdateDate,reviewId+"reviewUpdateDate,reviewId");
		return test.getReviewsByKeySet(reviewUpdateDate, reviewId);
	}
	@RequestMapping("/getReviewsForMap")
	public List<ReviewDTO> getReviewsForMap() {
		return test.getReviewsForMap();
	}

	@RequestMapping("/deleteReviews")
	public void deleteReviews(ReviewDTO reviewDTO) {
		List<String> reviewIds = reviewDTO.getReviewIds();

		log.error("{}===>",reviewIds+"reviewIds");
		test.deleteReviews(reviewIds);
	}

//	@RequestMapping("/delete")
//	public void deleteFiles(ReviewDTO reviewDTO) {
//		List<String> fileIds = reviewDTO.getFileIds();
//		if (fileIds == null || fileIds.isEmpty()) {
//			return;
//		}
//		test.deleteFiles(fileIds);
//	}
//
//	@RequestMapping("/get")
//	public void saveFiles(ReviewDTO reviewDTO) throws IOException {
//		String reviewId = reviewDTO.getEmail();
//	}
	@RequestMapping("data/review")
	public ReviewDTO reviewAppend(@ModelAttribute ReviewDTO review ){

		ReviewDTO data = review;
		log.error("추가된 리뷰는 : {}",data);

		return data;
	}
}
