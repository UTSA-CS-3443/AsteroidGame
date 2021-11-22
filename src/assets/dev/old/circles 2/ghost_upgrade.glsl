//this code can be run on https://www.shadertoy.com/new

const float SCALE = 16.0;

float square(float x) { return x * x; }

float unmix(float a, float b, float f) { return (f - a) / (b - a); }

void mainImage(out vec4 fragColor, in vec2 fragCoord) {
	vec2 uv = mod((floor(fragCoord) + vec2(0.5)) / SCALE, vec2(32.0));
	const vec3 blue = vec3(0.25, 0.375, 1.0);

	vec3 color;
	if (uv.y > 24.0) { //head
		if (distance(uv, vec2(16.0, 24.0)) < 8.0) { //inside head
			if (distance(vec2(abs(uv.x - 16.0), uv.y), vec2(3.0, 27.0)) < 2.0) { //eyes
				color = blue * 0.5;
			}
			else { //not eyes
				color = blue;
			}
		}
		else { //outside head
			color = vec3(0.0);
		}
	}
	else { //body
		float frac = square(unmix(24.0, 0.0, uv.y));
		float width = mix(8.0, 16.0, frac);
		float scaled = abs(uv.x - 16.0) / width;
		if (scaled < 1.0) { //inside body
			color = blue * mix(1.0, cos(scaled * (96.0 * 3.14159265359 / 32.0)) * 0.25 + 0.75, frac);
		}
		else { //outside body
			color = vec3(0.0);
		}
	}

	fragColor = vec4(color, 1.0);
}