var $centerContainer = $('.content-body');
var $breadcrumb = $('.breadcrumb');


NProgress.start();
$('.center-content-box').append(mask);


$('.user-panel-head').mouseover(function () {
    $('.user-panel-menu').slideDown(150);
});
$('.user-panel-head').mouseleave(function () {
    $('.user-panel-menu').slideUp(100);
});
$('.menu-item-level-two-title').click(function () {
    $('.menu-item-level-two-list').eq($('.menu-item-level-two-title').index(this)).slideToggle(200);
});

//default load index page
$(document).ready(function () {
    $centerContainer.load("html/adminindex.html", function () {
        NProgress.done();
        $(mask).remove();
    });

    $('.school_info').click(function () {
        $breadcrumb.html('<span>你当前的位置：</span>\n' +
            '<a class="section"><i class="fa fa-home"></i>首页</a>' +
            '<span>/学校信息</span>');
        $(mask).fadeOut();
        $centerContainer.load("html/school.html", function () {
            NProgress.done();
            $(mask).remove();
        });
    });


});