package com.smart.project.web.home.act;


import com.smart.project.proc.Test;
import com.smart.project.web.home.vo.*;
import com.smart.project.proc.Test;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.springframework.ui.Model;


import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.io.*;

import java.util.*;
@SessionAttributes("pageNum")
@Slf4j
@RestController
@RequiredArgsConstructor
public class HomeDataAct {
	final private Test test;

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
		PageMaker pageMaker = new PageMaker();
		pageMaker.setCri(cri);
		pageMaker.setTotalCount(totalCount);
		result.put("page",pageMaker);

		return result;
	}



	@PostMapping("/data/map")
	public List<MangoVO>getMapData(@RequestBody Map param){
		String name = String.valueOf(param.get("name"));
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
		vo.setUseremail(useremail);
		vo.setPlacename(placename);
		test.wishDelete(vo);
		WishListVO data = vo;

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
		test.reviewCount(dto.getTitle(), -1);
		test.deleteReply(dto);
	}
	// 해당 이메일로 로그인되었을때 리뷰 변경
	@RequestMapping("updateReview")
	public void updateReview(@ModelAttribute ReviewDTO reviewDTO){
		test.updateReview(reviewDTO);
	}

	@PostMapping("/idCheck")
	public int checkDuplicateId(@RequestBody Map param){
		String id = String.valueOf(param.get("userId"));
		int idCount = test.idCount(id);

		return idCount;
	}
	@PostMapping("/saveReview")
	public ReviewDTO saveReview(MultipartHttpServletRequest request) throws IOException {
		MultipartHttpServletRequest multi = request;
		List<MultipartFile> file = multi.getFiles("file");
		String id = request.getParameter("email");
		String title = request.getParameter("title");
		int grade = Integer.parseInt(request.getParameter("grade"));
		String review = request.getParameter("review");
		ReviewDTO reviewDTO = new ReviewDTO();
		reviewDTO.setEmail(id);
		reviewDTO.setTitle(title);
		reviewDTO.setGrade(grade);
		reviewDTO.setReview(review);
		if (!file.isEmpty()){
			byte[] img = (file.get(0).getBytes());
			if(img!=null){
				reviewDTO.setImg(img);
			}
		}else{
			reviewDTO.setImg(null);
		}
		test.reviewCount(title, 1);
		test.saveReview(reviewDTO);

		ReviewDTO data = reviewDTO;
		return data;
	}


	@PostMapping("showReview")
	public Map<String, Object> showReview(@ModelAttribute ReviewDTO review1,HttpServletRequest request) {
		Map<String, Object> data = new HashMap<>();
		String placename = review1.getTitle() ;

		String useremail = (String)request.getSession().getAttribute("email");
		List<ReviewDTO> review = test.currentReview(placename);
		data.put("review",review);
		data.put("session",useremail);
		log.error("가져온 리뷰데이터는 {} ",data);

		return data;
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

}
