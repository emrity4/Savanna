precision mediump float;

uniform sampler2D uTexture;
uniform vec2      uResolution;
uniform float     uTime;
uniform float     uOpacity;
uniform float     uBlur;
uniform float     uDisplacement;
uniform float     uSaturation;
uniform float     uAberration;
uniform float     uCornerRadius;

varying vec2 vTexCoord;

vec3 saturate(vec3 c, float s) {
    float l = dot(c, vec3(0.299, 0.587, 0.114));
    return mix(vec3(l), c, s);
}

void main() {
    vec2 uv = vTexCoord;

    // corner‑radius mask
    vec2 cr  = vec2(uCornerRadius);
    vec2 d   = max(cr - min(uv * uResolution, uResolution - uv * uResolution), 0.0);
    float crAlpha = 1.0 - smoothstep(0.0, 1.5, length(d));
    if (crAlpha < 0.01) discard;

    // animated displacement (liquid wobble)
    float t = uTime * 0.4;
    vec2 disp = vec2(
        sin(uv.y * 120.0 + t)          * uDisplacement / uResolution.x,
        cos(uv.x * 100.0 + t * 1.3)    * uDisplacement / uResolution.y
    );
    vec2 duv = uv + disp;

    // 3×3 box blur
    vec2 off   = uBlur / uResolution;
    vec3 color = vec3(0.0);
    for (float y = -1.0; y <= 1.0; y += 1.0)
        for (float x = -1.0; x <= 1.0; x += 1.0)
            color += texture2D(uTexture, duv + off * vec2(x, y)).rgb;
    color /= 9.0;

    // chromatic aberration
    float ab = uAberration / 1000.0;
    float r  = texture2D(uTexture, duv + vec2(ab, 0.0)).r;
    float b  = texture2D(uTexture, duv - vec2(ab, 0.0)).b;
    color = vec3(r, color.g, b);

    // saturation
    color = saturate(color, uSaturation);

    gl_FragColor = vec4(color, uOpacity * crAlpha);
}
