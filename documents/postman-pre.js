/**
 * @param host not include port
 */
const accessKeyEnv = 'AccessKey';
const secretKeyEnv = 'SecretKey';
const hostEnv = 'HmacHost';
const algorithm = 'HmacSHA256';

/**
 *
 * @param method
 * @param protocol
 * @param host          not include port
 * @param path
 * @param query         blank not include `?`
 * @param content_type
 * @param body
 * @param nonce
 * @param accessKey
 * @param secretKey
 * @returns {*|string}
 */
function signature({method, protocol, host, path, query, content_type, body, nonce}, {accessKey, secretKey}) {
    const plainText =
        method + '\n'
        + protocol + '\n'
        + host + '\n'
        + path + '\n'
        + query + '\n'
        + content_type + '\n'
        + body + '\n'
        + nonce;
    const signatureBytes = CryptoJS.HmacSHA256(plainText, secretKey);
    const signatureBase64 = CryptoJS.enc.Base64.stringify(signatureBytes);
    const sign = {
        plainText,
        signature: signatureBase64
    };
    console.info(`sign:${JSON.stringify(sign)}`);
    return signatureBase64;
}

function getBody(request) {
    // postman RequestBody
    const body = request.body;
    if (body) {
        // mode: raw, formdata, urlencoded, file
        if (body.mode === 'raw') {
            return body.raw;
        }
        if (body.mode === 'urlencoded') {
            const paramList = [];
            body.urlencoded.each((param) => {
                paramList.push(`${param.key}=${encodeURIComponent(param.value)}`);
            });
            return paramList.join("&");
        }
        throw new Error(`not support RequestBody.mode:${body.mode}`);
    }
    return '';
}

function getQuery(request) {
    if (request.url.query.count() === 0) {
        return "";
    }
    const paramList = [];
    request.url.query.each((param) => {
        paramList.push(`${param.key}=${encodeURI(param.value)}`);
    });
    return "?" + paramList.join("&");
}

const method = pm.request.method;
const protocol = pm.request.url.protocol;
const path = '/' + pm.request.url.path.join('/');
const query = getQuery(pm.request);
const content_type = pm.request.headers.get('Content-Type') || '';
// host not include port
const host = (pm.environment.get(hostEnv) || '').split(":")[0];
const nonce = Date.now() + '';
const body = getBody(pm.request);
const accessKey = pm.variables.get(accessKeyEnv) || '';
const secretKey = pm.variables.get(secretKeyEnv) || '';

const signatureStr = signature(
    {method, protocol, host, path, query, content_type, body, nonce},
    {accessKey, secretKey}
);
const authorization = algorithm + ": " + signatureStr;

// 设置签名和 Nonce
pm.request.headers.upsert({key: 'Authorization', value: authorization});
pm.request.headers.upsert({key: 'Nonce', value: nonce});
