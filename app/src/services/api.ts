import { toast } from "sonner";

export interface GenrePrediction {
	genre: string;
	popProbability: number;
	countryProbability: number;
	bluesProbability: number;
	jazzProbability: number;
	reggaeProbability: number;
	rockProbability: number;
	hipHopProbability: number;
	rnbProbability: number;
}

export async function predictLyrics(
	lyrics: string
): Promise<GenrePrediction | null> {
	const cleanedLyrics = clean_lyrics(lyrics);

	try {
		const response = await fetch("http://localhost:9090/lyrics/predict", {
			method: "POST",
			headers: {
				"Content-Type": "text/plain",
			},
			body: cleanedLyrics,
		});

		console.log(response);

		if (!response.ok) {
			throw new Error("Failed to predict lyrics");
		}

		return await response.json();
	} catch (error) {
		console.error("Error predicting lyrics:", error);
		toast.error("Failed to analyze lyrics. Is the server running?");
		return null;
	}
}

const clean_lyrics = (lyrics: string): string => {
	// Remove any extra spaces
	let cleanedLyrics = lyrics.replace(/\s+/g, " ").trim();

	// Remove any leading or trailing spaces
	cleanedLyrics = cleanedLyrics.replace(/^\s+|\s+$/g, "");

	// convert to lowercase
	cleanedLyrics = cleanedLyrics.toLowerCase();

	// remove [] and () and their contents
	cleanedLyrics = cleanedLyrics.replace(/\[.*?\]|\(.*?\)/g, "");

	// remove newlines
	cleanedLyrics = cleanedLyrics.replace(/(\r\n|\n|\r)/gm, " ");

	// remove redundant spaces
	cleanedLyrics = cleanedLyrics.replace(/\s+/g, " ");

	return cleanedLyrics;
};
