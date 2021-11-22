//this code can be run on https://www.shadertoy.com/new

void mainImage(out vec4 fragColor, in vec2 fragCoord) {
	vec2 uv = mod(floor(fragCoord) + vec2(0.5), vec2(64.0));

	float brightness;
	float dist = distance(uv, vec2(32.0));
	if (dist > 32.0) {
		brightness = 0.0;
	}
	else if (dist > 28.0) {
		brightness = smoothstep(32.0, 28.0, dist);
	}
	else if (dist > 24.0) {
		brightness = smoothstep(24.0, 28.0, dist);
	}
	else {
		brightness = 0.0;
	}
	vec3 color = vec3(brightness);

	fragColor = vec4(color, 1.0);
}