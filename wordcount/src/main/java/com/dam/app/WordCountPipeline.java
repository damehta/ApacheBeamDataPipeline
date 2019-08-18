package com.dam.app;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Hello world!
 *
 */
public class WordCountPipeline
{
    public static void main( String[] args )
    {
//        Create pipeline
        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline pipeline = Pipeline.create(options);

//        Read from text file

        ClassLoader classLoader = WordCountPipeline.class.getClassLoader();

            PCollection<String> lines = pipeline.apply("Read from file", TextIO.read().from("/tmp/input.txt"));
//        split each line into words
        PCollection<List<String>> wordsPerLine = lines.apply(MapElements.via(new SimpleFunction<String, List<String>>() {
            @Override
            public List<String> apply(String input) {
                return Arrays.asList(input.split(" "));
            }
        }));
        PCollection<String> words = wordsPerLine.apply(Flatten.iterables());

//        count the number of times each word occurred
        PCollection<KV<String, Long>> wordCount = words.apply(Count.perElement());

        wordCount.apply(MapElements.via(new SimpleFunction<KV<String, Long>, String>() {
            @Override
            public String apply(KV<String, Long> input) {
                return String.format("%s : %s", input.getKey(), input.getValue());
            }
        }))
                .apply(TextIO.write().to("output"));

        pipeline.run().waitUntilFinish();
    }
}
