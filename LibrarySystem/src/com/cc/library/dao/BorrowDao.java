package com.cc.library.dao;

import java.util.List;

import com.cc.library.domain.BorrowInfo;
import com.cc.library.domain.PageBean;
import com.cc.library.domain.Reader;

public interface BorrowDao {

	public PageBean<BorrowInfo> findBorrowInfoByPage(int pageCode, int pageSize);

	public BorrowInfo getBorrowInfoById(BorrowInfo info);

	public int addBorrow(BorrowInfo info);

	public List<BorrowInfo> getBorrowInfoByReader(Reader reader);

}