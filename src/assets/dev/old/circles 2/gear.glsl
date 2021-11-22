//this code can be run on https://www.shadertoy.com/new

void mainImage(out vec4 fragColor, in vec2 fragCoord) {
	vec2 uv = mod((floor(fragCoord) + 0.5) / 4.0, 64.0) - 32.0;
	float angle = atan(uv.y, uv.x);
	float radius = length(uv);

	float brightness;
	if (radius < 12.0) {
		brightness = 0.0;
	}
	else if (radius < cos(angle * 12.0) * 4.0 + 28.0) {
		brightness = 0.875;
	}
	else {
		brightness = 0.0;
	}
	vec3 color = vec3(brightness);

	fragColor = vec4(color, 1.0);
}