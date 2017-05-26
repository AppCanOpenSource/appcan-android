var Service = new MVVM.Service({
    pretreatment: function(data, option) {
        return data;
    },
    dosuccess: function(data, option) {
        return data;
    },
    doerror: function(e, err, option) {
        return err;
    },
    validate: function(data, option) {
        return 0;
    },
    ajaxCall: function(data, option) {
        var self = this;
        appcan.request.ajax({
            url: "",
            type: "GET",
            data: this.pretreatment(data, option),
            dataType: "",
            contentType: "application/x-www-form-urlencoded",
            success: function(data) {
                var res = self.validate(data, option);
                if (!res) option.success(self.dosuccess(data, option));
                else option.error(self.doerror(data, res, option));
            },
            error: function(e, err) {
                option.error(self.doerror(e, err, option));
            }
        });
    }
});

var Model = new(MVVM.Model.extend({

    initialize: function() {
        return;
    },
    parse: function(data) {
        return data;
    },
    validate: function(attrs, options) {
        return;
    },
    computeds: {},
    sync: function(method, model, options) {
        switch (method) {
        case "create":

            break;
        case "update":

            break;
        case "patch":

            break;
        case "read":
            Service.request({},
            options);
            break;
        case "delete":

            break;
        default:
            break;
        }
    }
}))()

var ViewModel = new(MVVM.ViewModel.extend({
    el: "#ScrollContent",

    initialize: function() {
        return;
    },
    model: Model,

}))();