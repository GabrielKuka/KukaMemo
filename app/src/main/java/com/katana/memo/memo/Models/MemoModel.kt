package com.katana.memo.memo.Models


class MemoModel(title: String, body: String, id: Int) {
    var memoTitle: String = title
    var memoBody: String = body
    var memoId = -3

    init {
        this.memoId = id
    }

}