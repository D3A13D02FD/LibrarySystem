package com.cc.library.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.struts2.ServletActionContext;

import net.sf.json.JSONObject;

import com.cc.library.dao.BookDao;
import com.cc.library.dao.BookTypeDao;
import com.cc.library.dao.BorrowDao;
import com.cc.library.dao.ForfeitDao;
import com.cc.library.domain.Admin;
import com.cc.library.domain.Book;
import com.cc.library.domain.BookType;
import com.cc.library.domain.BorrowInfo;
import com.cc.library.domain.ForfeitInfo;
import com.cc.library.domain.PageBean;
import com.cc.library.service.BookService;

public class BookServiceImpl implements BookService{

	private BookDao bookDao;

	private BookTypeDao bookTypeDao;
	private BorrowDao borrowDao;
	private ForfeitDao forfeitDao;
	
	public void setBorrowDao(BorrowDao borrowDao) {
		this.borrowDao = borrowDao;
	}

	


	
	/**
	 * @param bookTypeDao the bookTypeDao to set
	 */
	public void setBookTypeDao(BookTypeDao bookTypeDao) {
		this.bookTypeDao = bookTypeDao;
	}





	public void setForfeitDao(ForfeitDao forfeitDao) {
		this.forfeitDao = forfeitDao;
	}




	public void setBookDao(BookDao bookDao) {
		this.bookDao = bookDao;
	}



	@Override
	public PageBean<Book> findBookByPage(int pageCode, int pageSize) {
		// TODO Auto-generated method stub
		return bookDao.findBookByPage(pageCode,pageSize);
	}

	@Override
	public boolean addBook(Book book) {
		// TODO Auto-generated method stub
		return bookDao.addBook(book);
	}


	@Override
	public Book getBookById(Book book) {
		// TODO Auto-generated method stub
		return bookDao.getBookById(book);
	}

	@Override
	public Book updateBookInfo(Book updateBook) {
		// TODO Auto-generated method stub
		return bookDao.updateBookInfo(updateBook);
	}

	@Override
	public PageBean<Book> queryBook(Book book, int pageCode, int pageSize) {
		// TODO Auto-generated method stub
		return bookDao.queryBook(book,pageCode,pageSize);
	}

	@Override
	public int deleteBook(Book book) {
		// TODO Auto-generated method stub
		//?????????????????????????????????????????????????????????????????????????????????????????????????????????,???????????????
		//???????????????????????????
		List<BorrowInfo> borrowInfos = borrowDao.getBorrowInfoByBook(book);
		for (BorrowInfo borrowInfo : borrowInfos) {
			if(!(borrowInfo.getState()==2 || borrowInfo.getState()==5)){
				return -1;//?????????????????????,????????????
			}
			//????????????????????????????????????
			ForfeitInfo forfeitInfo = new ForfeitInfo();
			forfeitInfo.setBorrowId(borrowInfo.getBorrowId());
			ForfeitInfo forfeitInfoById = forfeitDao.getForfeitInfoById(forfeitInfo);
			if(forfeitInfoById!=null){
				if(forfeitInfoById.getIsPay()==0){
					return -2;//?????????????????????
				}
			}
		}
		boolean deleteBook = bookDao.deleteBook(book);
		if(deleteBook){
			return 1;
		}
		return 0;
	}




	@Override
	public JSONObject batchAddBook(String fileName, Admin admin) {
		List<Book> books = new ArrayList<Book>();
		List<Book> failBooks = new ArrayList<Book>();
		String str[] = new String[]{"??????ISBN???","????????????","????????????","????????????","?????????","??????","??????","??????"};
		// TODO Auto-generated method stub
		String realPath = ServletActionContext.getServletContext().getRealPath(fileName);
		 //??????workbook
        try {
			Workbook workbook = Workbook.getWorkbook(new File(realPath));
			//????????????????????????sheet
            Sheet sheet = workbook.getSheet(0);
            
            if(sheet.getColumns()!=8 ){
            	JSONObject jsonObject = new JSONObject();
            	jsonObject.put("error","???????????????,??????????????????" );
            	jsonObject.put("state","-1" );
            	return jsonObject;
            }else{
            	  //????????????
              
                for (int j = 0; j < sheet.getColumns(); j++) {
                    Cell cell = sheet.getCell(j,0);
                    if(!cell.getContents().equals(str[j])){
                    	JSONObject jsonObject = new JSONObject();
                    	jsonObject.put("error","???????????????,??????????????????" );
                    	jsonObject.put("state","-1" );
                    	return jsonObject;
                    }
                }
                
            }
            
               
            //??????????????????????????????????????????????????????

            //????????????
            for (int i = 1; i < sheet.getRows(); i++) {
              

           	
                String ISBN = sheet.getCell(0,i).getContents().trim();
                String type = sheet.getCell(1,i).getContents().trim();
                String bookName = sheet.getCell(2,i).getContents().trim();
                String author = sheet.getCell(3,i).getContents().trim();
                String publish = sheet.getCell(4,i).getContents().trim();
                String price = sheet.getCell(5,i).getContents().trim();
                String num = sheet.getCell(6,i).getContents().trim();
                String description = sheet.getCell(7,i).getContents().trim();
                if("".equals(ISBN) && "".equals(type) && "".equals(bookName) && "".equals(author) && "".equals(publish) && "".equals(price) && "".equals(num) && "".equals(description)){
                	//???????????????????????????
                	continue;
                }
                
                Book book = new Book();
                book.setISBN(ISBN);
                book.setBookName(bookName);
                book.setAutho(author);
                book.setDescription(description);
                book.setPress(publish);
                BookType bookType = new BookType();
                bookType.setTypeName(type);
                book.setBookType(bookType);
                
                try {  
                    if(Double.parseDouble(price)<=0){
                    	//??????????????????
                    	book.setPrice(new Double(-1));
                    	failBooks.add(book);
                    	continue ;
                    }
                    book.setPrice(Double.parseDouble(price));
                } catch (NumberFormatException e) { 
                    //??????????????????
                	book.setPrice(new Double(-1));
                	failBooks.add(book);
                	continue ;
                }  
                
                
                
                try {  
                    if(Integer.parseInt(num)<=0){
                    	//?????????????????????
                    	book.setNum(-1);
                    	failBooks.add(book);
                    	continue ;
                    }
                    book.setNum(Integer.parseInt(num));
                    book.setCurrentNum(Integer.parseInt(num));
                } catch (NumberFormatException e) {  
                    //??????????????????
                	book.setNum(-1);
                	failBooks.add(book);
                	continue ;
                }  
                
                
            	if("".equals(ISBN) || "".equals(bookName) || "".equals(type)){
	        		//?????????3?????????????????????????????????????????????????????????
            		//????????????????????????
            		failBooks.add(book);
	        		continue;
            	}
                
                //???????????????????????????????????????????????????
                BookType typeByName = bookTypeDao.getBookTypeByName(bookType);
                if(typeByName==null){
                	//???????????????????????????????????????????????????,????????????
                	//????????????????????????
                	bookType.setTypeName(bookType.getTypeName() + "(???????????????)");
            		failBooks.add(book);
                	continue;
                }
                
                book.setBookType(typeByName);
                //????????????????????????,??????????????????????????????????????????????????????
                book.setPutdate(new Date(System.currentTimeMillis()));
                book.setAdmin(admin);
                books.add(book);
            }
            workbook.close();
            int success = bookDao.batchAddBook(books,failBooks);
            
            JSONObject jsonObject = new JSONObject();
            if(failBooks.size() != 0){
            	 //????????????????????????excel??????
                String exportExcel = exportExcel(failBooks);
                jsonObject.put("state", "2");
                jsonObject.put("message","??????" + success + "???,??????" + failBooks.size() + "???");
                jsonObject.put("failPath", "admin/FileDownloadAction.action?fileName=" + exportExcel);
                return jsonObject;
            }else{
            	 jsonObject.put("state", "1");
                 jsonObject.put("message","??????" + success + "???,??????" + failBooks.size() + "???");
                 return jsonObject;
            }
            
            
            
            
		} catch (Throwable e) { 
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}





	/**
	 * ??????excel??????
	 * @param failReaders ???????????????
	 * @return ??????????????????
	 */
	public String exportExcel(List<Book> failBooks){
		//?????????????????????
        String[] title = {"??????ISBN???","????????????","????????????","????????????","?????????","??????","??????","??????"};
        String path = ServletActionContext.getServletContext().getRealPath("/download");
        String fileName = +System.currentTimeMillis()+"_failBooks.xls";
        //??????Excel??????
        File file = new File(path, fileName);
        try {
            file.createNewFile();
            //???????????????
            WritableWorkbook workbook = Workbook.createWorkbook(file);
            WritableSheet sheet = workbook.createSheet("sheet1", 0);

            Label label = null;

            //?????????????????????
            for(int i = 0; i<title.length; i++){
                label = new Label(i, 0, title[i]);
                sheet.addCell(label);
            }

            //????????????
            for(int i = 1; i<=failBooks.size(); i++){
                label = new Label(0, i, failBooks.get(i-1).getISBN());
                sheet.addCell(label);
                label = new Label(1, i, failBooks.get(i-1).getBookType().getTypeName());
                sheet.addCell(label);
                label = new Label(2, i, failBooks.get(i-1).getBookName());
                sheet.addCell(label);
                label = new Label(3, i, failBooks.get(i-1).getAutho());
                sheet.addCell(label);
                label = new Label(4, i, failBooks.get(i-1).getPress());
                sheet.addCell(label);
                
                if(failBooks.get(i-1).getPrice()==null){
                	label = new Label(5, i, "????????????");
                    sheet.addCell(label);
                }else if(failBooks.get(i-1).getPrice().equals(new Double(-1))){
                	 label = new Label(5, i, "????????????");
                     sheet.addCell(label);
                }else{
                	 label = new Label(5, i, failBooks.get(i-1).getPrice().toString());
                     sheet.addCell(label);
                }
                if(failBooks.get(i-1).getNum()==null){
                	label = new Label(6, i, "????????????");
                    sheet.addCell(label);
                }else if(failBooks.get(i-1).getNum().equals(-1)){
                	 label = new Label(6, i, "????????????");
                     sheet.addCell(label);
                }else{
               	 label = new Label(6, i, failBooks.get(i-1).getNum().toString());
                 sheet.addCell(label);
                }
                label = new Label(7, i, failBooks.get(i-1).getDescription());
                sheet.addCell(label);
            }
            //????????????
            workbook.write();

            workbook.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		return fileName;
	}





	@Override
	public String exportBook() {
		List<Book> findAllBooks = bookDao.findAllBooks();
		String exportBookExcel = exportBookExcel(findAllBooks);
		return "admin/FileDownloadAction.action?fileName=" + exportBookExcel;
	}
	
	
	
	
	/**
	 * ?????????????????????excel??????
	 * @param ???????????????
	 * @return ??????????????????
	 */
	public String exportBookExcel(List<Book> books){
		//?????????????????????
        String[] title = {"??????ISBN???","????????????","????????????","????????????","?????????","??????","??????","????????????","????????????","???????????????","??????"};
        String path = ServletActionContext.getServletContext().getRealPath("/download");
        String fileName = +System.currentTimeMillis()+"_allBooks.xls";
        //??????Excel??????
        File file = new File(path, fileName);
        try {
            file.createNewFile();
            //???????????????
            WritableWorkbook workbook = Workbook.createWorkbook(file);
            WritableSheet sheet = workbook.createSheet("sheet1", 0);

            Label label = null;

            //?????????????????????
            for(int i = 0; i<title.length; i++){
                label = new Label(i, 0, title[i]);
                sheet.addCell(label);
            }

            //????????????
            for(int i = 1; i<=books.size(); i++){
                label = new Label(0, i, books.get(i-1).getISBN());
                sheet.addCell(label);
                label = new Label(1, i, books.get(i-1).getBookType().getTypeName());
                sheet.addCell(label);
                label = new Label(2, i, books.get(i-1).getBookName());
                sheet.addCell(label);
                label = new Label(3, i, books.get(i-1).getAutho());
                sheet.addCell(label);
                label = new Label(4, i, books.get(i-1).getPress());
                sheet.addCell(label);
                label = new Label(5, i, books.get(i-1).getPrice().toString());
                sheet.addCell(label);
                label = new Label(6, i, books.get(i-1).getNum().toString());
                sheet.addCell(label);
                label = new Label(7, i, books.get(i-1).getCurrentNum().toString());
                sheet.addCell(label);
                label = new Label(8, i, books.get(i-1).getPutdate().toLocaleString());
                sheet.addCell(label);
                label = new Label(9, i, books.get(i-1).getAdmin().getName());
                sheet.addCell(label);
                label = new Label(10, i, books.get(i-1).getDescription());
                sheet.addCell(label);
                
            }
            //????????????
            workbook.write();

            workbook.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		return fileName;
	}

}
