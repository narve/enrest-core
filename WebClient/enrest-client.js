const propValAttrs = [
    'content',
    'value',
    'datetime',
    'src',
];

function toString(e) {
    if (e.hasAttribute('content'))
        throw 'content: ' + e;
    for (const attr of propValAttrs) {
        if (e.hasAttribute(attr)) {
            log("attr: " + attr);
            return e.attributes[attr].value;
        }
    }
    // Hack: Not proper according to MicroData spec
    const contentHolder = e.querySelector('[content]');
    if (contentHolder)
        return contentHolder.attributes['content'].value;
    return e.textContent.trim();
}

function toObject(e) {
    if (!e.hasAttribute('itemscope')) {
        throw 'no itemscope for ' + e.tagName + " " + Array.from(e.attributes).map(a => a.name + "=" + a.value).join(",");
    }
    if (!e.hasAttribute('itemtype')) {
        throw 'no itemtype';
    }
    const o = {
        type: e.attributes['itemtype'].value,
    };
    Array.from(e.querySelectorAll('[itemprop]'))
        .forEach(prop => o[prop.attributes['itemprop'].value] = toAny(prop))
    ;

    return o;
}

function toAny(e) {
    return e.hasAttribute('itemscope') ? toObject(e) : toString(e);
}

function log(msg, toReturn) {
    console.log(msg);
    return toReturn;
}

function init() {
    fetch('https://localhost:5001/item', {
        headers: {
            "Accept": "text/html",
        }
    }).then(r => log('Got response!', r))
        .then(r => r.text())
        .then(t => log('Got text: ' + t, t))
        .catch(e => log('Failed: ' + e, e))
        .finally(() => log('done fetching'));
}

function parseHtml(s, url = null) {
    // log('Parsing html: ', s);
    const allElements = new DOMParser().parseFromString(s, 'text/html');
    const dataElements = Array.from(allElements.querySelectorAll('.pv-data > [itemscope]'));
    const dataObject = dataElements.map(e => toObject(e));
    log(`Done parsing ${url} ${dataObject.length} objects: `, dataObject);
    return dataObject;
}


document.addEventListener("DOMContentLoaded", () => init());