(function(context) {
  document.loadWidget = function(widget) {
    const renderFunctionName = widget.renderFunctionName;
    const instanceId = widget.instanceId;
    const formid = widget.formid;
    const locale = widget.locale;
    const triggerManually = widget.triggerManually;
    const onRenderFinish = widget.onRenderFinish;
    const bpid = widget.bpid;
    window.urxbpid = bpid;
    window.urxEnvironment = widget.environment;
    window.onUrxFormSubmit = widget.onUrxFormSubmit; // trigger after submit before redirect
    window.urxFormLoaded = widget.formLoaded; // trigger this when form loaded.
    const preFillData = widget.preFillData;
    if(typeof widget.onUrxFormSubmitSuccess === 'function' && typeof window.onUrxFormSubmitSuccess !=='function'){
      window.onUrxFormSubmitSuccess = widget.onUrxFormSubmitSuccess;
    }
    const userActionEvent = widget.userActionEvent || function (){}
    window.urxUserActionEvent = userActionEvent; // trigger this when form loaded.
    console.log('loader:', window.urxEnvironment);
    const design = widget.design || {
      column: 1,
      theme: 'light',
      singleStep: false,
    };
    if (typeof document.addEventListener !== 'function') {
      return;
    }
    try {
      if (triggerManually){
        context[renderFunctionName](instanceId, formid, locale, design, onRenderFinish, bpid, preFillData);
      }else{
        document.addEventListener("DOMContentLoaded", () => {
          if (typeof context[renderFunctionName] !== 'function') {
            return;
          }
          context[renderFunctionName](instanceId, formid, locale, design, onRenderFinish, bpid, preFillData);
        });
      }

    }
    catch(err) {
      console.error(err.message());
    }
  };
  document.loadWidgets = function(widgets) {
    if (!Array.isArray(widgets)) {
      return;
    }
    widgets.forEach(document.loadWidget);
  };
})(window);
