var uexCallback = {
    queue: [],
    callback: function () {
        var params = Array.prototype.slice.call(arguments, 0);
        var id = params.shift();
        var permanent = params.shift();
        this.queue[id].apply(this, params);
        if (!permanent) {
            delete this.queue[id];
        }
    }
};

function fo() {
    var args_all = Array.prototype.slice.call(arguments, 0);
    var uexName = args_all[0];
    var method = args_all[1];
    var args = Array.prototype.slice.call(args_all[2], 0);
    var aTypes = [];
    for (var i = 0; i < args.length; i++) {
        var arg = args[i];
        var type = typeof arg;
        if (type == "function") {
            var callbackID = uexCallback.queue.length;
            uexCallback.queue[callbackID] = arg;
            args[i] = callbackID;
        }
        aTypes[aTypes.length] = type;
    }
    var result = JSON.parse(prompt('AppCan_onJsParse:' + JSON.stringify({
            uexName: uexName,
            method: method,
            args: args,
            types: aTypes
        })));
    if (result.code != 200) {
        console.log("method call error, code:" + result.code + ", message: " +
            result.result);
    }
    var content = result.result;
    if (content.funcInfos != null) {
        for (var name in content.funcInfos) {
            var targetFunc = content.funcInfos[name];
            content[name] = function () {
                var params = Array.prototype.slice.call(arguments, 0);
                if (content.ext != null) {
                    params[params.length] = content.ext;
                }
                window[uexName][targetFunc].apply(uexName, params);
            };
        }
        delete content.funcInfos;
    }
    return content;
}
window.uexDispatcher = {};
uexDispatcher.dispatch = function () {
    return fo(arguments[0], arguments[1], arguments[2]);
};
