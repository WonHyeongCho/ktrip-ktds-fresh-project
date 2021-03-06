package com.ktds.ktrip.controller;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ktds.ktrip.dao.UserDAO;
import com.ktds.ktrip.domain.UserVO;
import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;

@WebServlet("/signupCheck")
public class SignUpController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		//파일이 저장될 서버의 경로
		String savePath = "/var/lib/tomcat8/webapps/user_img";
		String defaultPhotoPath="/var/lib/tomcat8/webapps/user_img/default.jpg";
		String picturePath;
		String dbPath;
		
		//파일 크기 15MB로 제한
		int sizeLimit = 1024*1024*15;

		//↓ request 객체,               ↓ 저장될 서버 경로,       ↓ 파일 최대 크기,    ↓ 인코딩 방식,       ↓ 같은 이름의 파일명 방지 처리
		//(HttpServletRequest request, String saveDirectory, int maxPostSize, String encoding, FileRenamePolicy policy)
		//아래와 같이 MultipartRequest를 생성만 해주면 파일이 업로드 된다.(파일 자체의 업로드 완료)
		MultipartRequest multi = new MultipartRequest(request, savePath, sizeLimit, "utf-8", new DefaultFileRenamePolicy());

		UserVO vo = new UserVO();
		vo.setId(multi.getParameter("id"));
		vo.setPwd(multi.getParameter("pwd"));
		vo.setEmail(multi.getParameter("email"));
		vo.setName(multi.getParameter("name"));
		vo.setSex(multi.getParameter("sex"));
		vo.setPhone_num(multi.getParameter("phone_num"));
		vo.setNational(multi.getParameter("country"));
		vo.setResidential_contry(multi.getParameter("residential_country"));
		if(multi.getFilesystemName("photo")==null) {
			dbPath="http://13.125.48.37:8080/user_img/default.jpg";
		}else {
			picturePath=savePath+"/"+multi.getParameter("id")+".jpg";
			/*
			 * 사진경로에서 파일을 불러오고 사진의 이름을 유저ID로 변경
			 */
			File file=new File(savePath+"/"+multi.getFilesystemName("photo"));
			file.renameTo(new File(picturePath));
			dbPath = "http://13.125.48.37:8080/user_img/" + multi.getParameter("id") + ".jpg";
		}

		System.out.println("저장경로 "+savePath);
		//System.out.println("사진경로 "+picturePath);
		System.out.println("폼에서 넘어오는 사진 이름 "+multi.getFilesystemName("photo"));
		
		System.out.println("이름변경완료");
		System.out.println(vo.toString());

		UserDAO dao = new UserDAO();
		int cheackvalue = dao.insertUser(vo, dbPath);

		if (cheackvalue == -1) {
			response.sendRedirect("/ktrip/signup-fail-action.jsp");
		}else {
			dao.login(vo);
			session.setAttribute("user_id", vo.getNum_id());
			request.setAttribute("success", "true");
			response.sendRedirect("/ktrip/index.jsp");
		}
	}
}
