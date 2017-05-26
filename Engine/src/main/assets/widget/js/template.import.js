var $template = {
    load : function(tpl) {
        var items = ( tpl ? $("[data-import]", $(tpl)) : $("[data-import]"));
        for (var i = 0; i < items.length; i++) {
            var item = $(items[i]);
            if (item.data("async") == true)
                continue;
            $.ajax({
                url : item.attr("data-import"),
                type : 'GET',
                timeout : 10000,
                async : false,
                success : function(data) {
                    var i = $(data);
                    item.html(i);
                    $template.load(i);
                },
                error : function(e) {
                }
            });
        }
    },
    loadByUrl : function(dom, url) {
        $.ajax({
            url : url,
            type : 'GET',
            timeout : 10000,
            async : false,
            success : function(data) {
                var i = $(data);
                dom.html(i);
                $template.load(i);
            },
            error : function(e) {
            }
        });
    },
    loadByDom :function(dom){
        this.loadByUrl(dom,$(dom).data("import"));
    }
}
_.extend($template, Backbone.Events);
$template.load();

