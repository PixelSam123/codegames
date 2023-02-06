package io.github.pixelsam123.problems;

import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ProblemService {

    private static final List<Problem> problems = List.of(
        new Problem(
            "Welcome!",
            """
                This is a sample question to get you started.
                Simply return `hello world` from the `welcome` function supplied in your editor, optionally add login information and hit submit!
                If you have entered credentials before hitting submit, you'll see your submission on the link above this challenge.""",
            "const welcome = () => {}",
            """
                function assertEquals(actual, expected, msg = 'Did not pass test!') {
                    if (actual !== expected) throw Error(`${msg}\\nExpected: ${expected}\\nActual: ${actual}`)
                }
                function test(actual, expected) {
                    assertEquals(welcome(), 'hello world')
                }
                test()"""
        ),
        new Problem(
            "Clamp Trolls",
            """
                Streamers are becoming really angry! They keep getting donation messages with repeated symbols like this:
                ```
                @@@@@@@@@@@@@@@@@@@ :):):):):):):)
                ``` \s
                The text-to-speech engines will read them out like 'at symbol at symbol at symbol at symbol..., smiley face smiley face'... you get the point.
                Your task is to create a function that will clamp down the trolls. It should handle:
                1. Obvious examples using symbols
                ```
                clampTrolls('@@@@@@@@@@@@@@@@@@@ :):):):):):):)', 4) // '@@@@ :):):):)'
                clampTrolls(':):):):):)', 2) // ':):)'
                clampTrolls('In-between normal @@@@@@@@@@ text', 3) // 'In-between normal @@@ text'
                ```
                2. Manual repetition of substrings
                ```
                clampTrolls('your smiley face smiley face smiley face smiley face', 2) // 'your smiley face smiley face'
                ```
                Please help them before they throw their phones in rage!""",
            "const clampTrolls = (str, maxLimit) => {}",
            """
                function assertEquals(actual, expected, msg = 'Did not pass test!') {
                    if (actual !== expected) throw Error(`${msg}\\nExpected: ${expected}\\nActual: ${actual}`)
                }
                function test(n, limit, expected) {
                    assertEquals(clampTrolls(n, limit), expected, `Input: ${n}, Limit: ${limit}`)
                }
                test('@@@@@@@', 4, '@@@@')
                test(':):):):):)', 2, ':):)')
                test('@@@@@ oke :):):):)', 3, '@@@ oke :):):)')
                test(
                    'menunggu dengan wajah tersenyum wajah tersenyum wajah tersenyum wajah tersenyum kamu',
                    3,
                    'menunggu dengan wajah tersenyum wajah tersenyum wajah tersenyum kamu'
                )
                test(
                    '@@@@@@@@@@@ akeong akeong akeong !!!! akeong @@@@@@ wajah tersenyum wajah tersenyum wajah tersenyum ###',
                    2,
                    '@@ akeong akeong !! akeong @@ wajah tersenyum wajah tersenyum ##'
                )"""
        )
    );

    public Uni<List<Problem>> getAll() {
        return Uni.createFrom().item(() -> problems);
    }

    public Uni<Problem> getOneByIdx(int idx) {
        return Uni.createFrom().item(() -> problems.get(idx));
    }

}
