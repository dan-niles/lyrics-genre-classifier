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
		pop: "settle cover cuddle hold arm heart chest lips press neck fall eye know feel forget kiss like wanna love wanna love wanna love feel like fall fall fall settle safety lady body warm cold wind blow hold arm heart chest lips press neck fall eye know feel forget kiss like wanna love wanna love wanna love feel like fall fall fall yeah feel hate truth guess know hold close help kiss like wanna love wanna love wanna love feel like fall fall fall kiss like wanna love wanna love wanna love feel like fall fall fall",
		rock: "close city tell people come death darkness rush forward bite wall break away listen fool rule rule kill spirit blind play burn finger lose hold flame begin listen fool rule break away listen fool break circle stop movement wheel throw grind remember start roll right fool rule",
		hiphop:
			"walk valley death look life realize leave blastin laughin long mama think mind go cross deserve treat like punk know unheard better watch talkin walkin homies line chalk hate gotta grow pistol smoke fool kinda little homies wanna like knees night sayin prayers streetlight spendin live livin gangsta paradise spendin live livin gangsta paradise spendin live livin gangsta paradise spendin live livin gangsta paradise situation facin lyric commercial",
		jazz: "Fly me to the moon Let me play among the stars Let me see what spring is like On Jupiter and Mars In other words, hold my hand In other words, baby, kiss me. Fill my heart with song And let me sing forevermore You are all I long for All I worship and adore In other words, please be true In other words, I love you",
		country:
			"walk world share dream need life search look long long time horizions come hand walk world walk world share dream search horizions come hand walk world come hand walk world",
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
