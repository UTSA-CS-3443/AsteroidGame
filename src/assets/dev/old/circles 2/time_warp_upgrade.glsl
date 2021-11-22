//this code can be run on https://www.shadertoy.com/new

const float SCALE = 4.0;

bool drawHand(vec2 uv, float angle, vec2 handSize) {
	angle *= 2.0 * 3.14159265359 / 12.0;
	vec2 target = vec2(cos(angle), sin(angle));
	vec2 perpendicular = vec2(target.y, -target.x);
	vec2 handCoord = vec2(dot(uv, perpendicular), dot(uv, target));

	if (handCoord.y < 0.0) {
		if (length(handCoord) < handSize.x) {
			return true;
		}
	}
	else if (handCoord.y > handSize.y) {
		if (length(handCoord - vec2(0.0, handSize.y)) < handSize.x) {
			return true;
		}
	}
	else if (abs(handCoord.x) < handSize.x) {
		return true;
	}
	return false;
}

void mainImage(out vec4 fragColor, in vec2 fragCoord) {
	fragColor = vec4(0.25, 0.375, 0.875, 1.0);
	#define color fragColor.rgb
	vec2 uv = mod((floor(fragCoord) + 0.5) / SCALE, vec2(64.0)) - vec2(32.0);
	float radius = length(uv);
	vec2 uvDir = uv / radius;
	float angle = atan(uv.y, uv.x) * (0.5 / 3.14159265359) + 0.5;
	angle = angle * 12.0 + 0.5;
	float fractAngle = fract(angle);
	float floorAngle = angle - fractAngle;

	const vec2 lightDir = normalize(vec2(-1.0, 1.0));
	float brightness = 1.0;
	for (int i = 0; i < 16; i++) {
		//big ticks
		if (mod(floorAngle, 3.0) == 0.0) {
			if (
				fractAngle == clamp(fractAngle, 0.25, 0.75) &&
				radius == clamp(radius, 22.0, 30.0)
			) {
				color = vec3(dot(uvDir, lightDir) * (radius - 26.0) / 3.0 * 0.25 + 0.25);
				color *= brightness;
				return;
			}
		}
		//small ticks
		{
			float roundedAngle = (floorAngle + 6.0) * (2.0 * 3.14159265359 / 12.0);
			vec2 target = vec2(cos(roundedAngle), sin(roundedAngle)) * 26.0;
			float offset = distance(uv, target);
			if (offset < 1.5) {
				color = vec3(dot(uv - target, lightDir) / 1.5 * 0.25 + 0.25);
				color *= brightness;
				return;
			}
		}
		//gold ring
		if (radius > 24.0 && radius < 28.0) {
			fragColor = vec4(1.0, 1.0, 0.0, 1.0);
			float light = dot(uvDir, lightDir) * (radius - 26.0) / 2.0;
			if (light > 0.0) color = mix(color, vec3(1.0), light);
			else color = mix(color, vec3(0.5, 0.5, 0.0), -light);
			color *= brightness;
			return;
		}
		//hand
		if (
			//second hand
			drawHand(uv, float(i) + 0.5, vec2(1.0, 20.0)) ||
			//minute hand
			drawHand(uv, float(i) + 5.5, vec2(0.75, 16.0)) ||
			//hour hand
			drawHand(uv, float(i) + 8.25, vec2(0.5, 12.0))
		) {
			color = vec3(0.0, brightness, brightness);
			return;
		}
		//face
		if (radius < 26.0) {
			color = vec3(0.9375 * brightness);
		}
		uv *= 1.25;
		radius *= 1.25;
		brightness /= 1.25;
	}
}