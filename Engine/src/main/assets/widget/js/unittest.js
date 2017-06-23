var UNIT_TEST = {
    units : [],
    testcase : null,
    addCase : function(name, items) {
        for (var i in items) {
            var item = {
                name : name,
                item : i,
                run : items[i]
            }
            this.units.push(item);
        }
    },
    result : {
        caseresult : [],
        success : 0,
        error : 0
    },
    assert : function(a) {
        var self = this;
        a ? (this.result.success++) : (this.result.error++);
        var item = {
            name : this.testcase.name,
            item : this.testcase.item,
            result : a
        }
        console.log(JSON.stringify(item));
        this.result.caseresult.push(item);
        var out = $("<div style='color:" + (item.result ? "black" : "red") + "'></div><br>");
        out.text("CASE " + item.name + "  " + item.item + "  " + (item.result ? "成功" : "失败"));
        this.dom.append(out);
        setTimeout(function() {
            self.trigger("_NEXTCASE", "");
        }, 100);
    },
    assertString : function(a) {
        var out = $("<pre>" + a + "</pre>");
        this.dom.append(out);
        this.assert(a);
    },
    assertRaises : function(a, b) {
        this.trigger("assertRaise", a, b);
    },
    assertTrue : function(a) {
        this.assert(a === true);
    },
    assertEqual : function(a, b) {
        this.assert(a === b);
    },
    assertNotEqual : function(a, b) {
        this.assert(a != b);
    },
    log : function(str) {
        var out = $("<div></div>");
        out.text("LOG: " + str);
        this.dom.append(out);
    },
    dom : function() {
        return this.dom;
    },
    _summary : function() {
        var out = $("<div></div>");
        out.text("CASE 成功 " + this.result.success);
        this.dom.append(out);
        out = $("<div></div>");
        out.text("CASE 失败" + this.result.error);
        this.dom.append(out);
    },
    start : function(dom) {
        var self = this;
        this.dom = dom || $("body");
        this.on("_NEXTCASE", function() {
            try {
                if (self.units.length == 0) {
                    self._summary();
                    return;
                }
                self.testcase = self.units.shift();
                var out = $("<div style='color:blue'> " + self.testcase.name + self.testcase.item + ":START CASE </div>");
                self.dom.append(out);
                self.testcase.run();
            } catch(e) {
                var out = $("<div style='color:red'>" + self.testcase.name + self.testcase.item + ":" + e.message + "</div>");
                self.dom.append(out);
            }
        });
        this.trigger("_NEXTCASE", "");
    }
}

_.extend(UNIT_TEST, Backbone.Events);
UNIT_TEST.on("assertRaise", function(err, data) {
})
