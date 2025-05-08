import React, { useState } from "react";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { toast } from "sonner";
import { MusicIcon } from "lucide-react";

interface LyricsInputProps {
	onSubmit: (lyrics: string) => void;
	isLoading: boolean;
}

const LyricsInput: React.FC<LyricsInputProps> = ({ onSubmit, isLoading }) => {
	const [lyrics, setLyrics] = useState("");

	const handleSubmit = (e: React.FormEvent) => {
		e.preventDefault();

		if (!lyrics.trim()) {
			toast.warning("Please enter some lyrics to analyze");
			return;
		}

		onSubmit(lyrics);
	};

	const sampleLyrics = {
		pop: "Baby, I'm dancing in the dark\nWith you between my arms\nBarefoot on the grass\nListening to our favorite song\nWhen you said you looked a mess\nI whispered underneath my breath\nBut you heard it, darling\nYou look perfect tonight",
		rock: "I see a red door\nAnd I want it painted black\nNo colors anymore\nI want them to turn black\nI see the girls walk by\nDressed in their summer clothes\nI have to turn my head\nUntil my darkness goes",
		hiphop:
			"His palms are sweaty, knees weak, arms are heavy\nThere's vomit on his sweater already, mom's spaghetti\nHe's nervous, but on the surface he looks calm and ready\nTo drop bombs, but he keeps on forgetting",
		jazz: "Fly me to the moon\nLet me play among the stars\nLet me see what spring is like\nOn Jupiter and Mars\nIn other words, hold my hand\nIn other words, baby, kiss me",
		country:
			"I've been a walking heartache\nI've made a mess of me\nThe person that I've been lately\nAin't who I wanna be\nBut you stay here right beside me\nWatch as the storm blows through\nAnd I need you",
	};

	const fillSampleLyrics = (genre: keyof typeof sampleLyrics) => {
		setLyrics(sampleLyrics[genre]);
		toast.info(
			`${genre.charAt(0).toUpperCase() + genre.slice(1)} lyrics loaded`
		);
	};

	return (
		<form onSubmit={handleSubmit} className="space-y-4">
			<div className="space-y-2">
				<label
					htmlFor="lyrics"
					className="text-sm font-medium text-muted-foreground"
				>
					Enter Song Lyrics
				</label>
				<Textarea
					id="lyrics"
					className="min-h-[200px] input-gradient border-border/50"
					placeholder="Paste song lyrics here..."
					value={lyrics}
					onChange={(e) => setLyrics(e.target.value)}
				/>
			</div>
			<Button
				type="submit"
				className="w-full bg-primary hover:bg-primary/80"
				disabled={isLoading}
			>
				<MusicIcon className="mr-2 h-4 w-4" />
				{isLoading ? "Analyzing..." : "Classify Genre"}
			</Button>

			<div className="pt-2">
				<p className="text-xs text-muted-foreground mb-2">
					Or try sample lyrics:
				</p>
				<div className="flex flex-wrap gap-2">
					<Button
						type="button"
						variant="outline"
						size="sm"
						onClick={() => fillSampleLyrics("pop")}
						className="flex-1"
					>
						⭐️ Pop
					</Button>
					<Button
						type="button"
						variant="outline"
						size="sm"
						onClick={() => fillSampleLyrics("rock")}
						className="flex-1"
					>
						🤘 Rock
					</Button>
					<Button
						type="button"
						variant="outline"
						size="sm"
						onClick={() => fillSampleLyrics("hiphop")}
						className="flex-1"
					>
						🎤 Hip Hop
					</Button>
					<Button
						type="button"
						variant="outline"
						size="sm"
						onClick={() => fillSampleLyrics("jazz")}
						className="flex-1"
					>
						🎷 Jazz
					</Button>
					<Button
						type="button"
						variant="outline"
						size="sm"
						onClick={() => fillSampleLyrics("country")}
						className="flex-1"
					>
						🤠 Country
					</Button>
				</div>
			</div>
		</form>
	);
};

export default LyricsInput;
