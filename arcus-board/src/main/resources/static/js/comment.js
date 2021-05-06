function showCmtList(pid, groupIndex, pageIndex) {
    $.ajax({
        url : "/comment",
        type : 'POST',
        dataType : 'json',
        data : JSON.stringify({'pid': pid, 'groupIndex' : groupIndex, 'pageIndex' : pageIndex}),
        contentType : 'application/json; charset=utf-8',
        success : function(data) {
            var html = "";
            var cmtList = data['cmtList'];
            var pagination = data['pagination']

            for (i=0; i<cmtList.length; i++) {
                html += "<div class='row'>"
                html += "<div class='col-md-8'>"
                html += "<td>" + cmtList[i].userName + "</td>"
                html += "<div class='w-100 mt-2'></div>"
                html += "<td>" + moment(cmtList[i].updatedDate).format('YYYY-MM-DD hh:mm:ss') + "</td>"
                html += "<div class='w-100 mt-2'></div>"
                html += "<td>" + cmtList[i].content + "</td>"
                html += "</div> <div class='col'><br/>"
                html += "<a class='btn btn-danger float-right mr-3 text-white' onclick='deleteCmt(" + cmtList[i].cid + ")'>삭제</a>"
                html += "<a class='btn btn-success float-right mr-3 text-white' onclick='editCmt(" + cmtList[i].cid + ")'>수정</a>"
                html += "</div> </div> <hr/>"
            }

            var pgHtml = "";

            var prev_groupIndex = pagination.groupIndex - 1;
            var prev_pageIndex = (pagination.groupIndex-2) * pagination.groupSize + 1;

            pgHtml += "<div id='pagination_add'> <div class='row'> <div class='col'> <ul class='pagination'>"
            if (pagination.prev == false) { pgHtml += "<li class='page-item disabled'> " }
            else { pgHtml += "<li>" }
            pgHtml += "<button class='page-link' onclick='showCmtList(" + pid + "," + prev_groupIndex + "," + prev_pageIndex + ")'>이전</button> </li>"

            for (var i=pagination.startRow; i<=pagination.endRow; i++) {
                if (pagination.pageIndex == i) { pgHtml += "<li class='page-item active'>" }
                else { pgHtml += "<li>" }
                pgHtml += "<button class='page-link' onclick='showCmtList(" + pid + "," + pagination.groupIndex + "," + i + ")'>" + i + "</button> </li>"

            }

            var next_groupIndex = pagination.groupIndex + 1;
            var next_pageIndex = pagination.groupIndex * pagination.groupSize + 1;

            if (pagination.next == false) { pgHtml += "<li class='page-item disabled'> " }
            else { pgHtml += "<li>" }
            pgHtml += "<button class='page-link' onclick='showCmtList(" + pid + "," + next_groupIndex + "," + next_pageIndex + ")'>다음</button> </li>"

            $('#commentList').html(html);
            $('#commentPaging').html(pgHtml);
        }
    })
}



