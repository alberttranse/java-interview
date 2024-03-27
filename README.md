# java-interview

# ls Command Simulation

Simulated options for `ls` command:

- `-a` : Include directory entries whose names begin with a dot (`.`).
- `-A` : Almost the same as `-a`, but does not include the implied `.` and `..` entries.
- `-d` : List directories themselves, not their contents.
- `-F` : Append a character to each file name indicating the file type.
- `-l` : Use a long listing format.
- `-r` : Reverse the order of the sort.
- `-S` : Sort files by size.
- `-t` : Sort by modification time, newest first.

Comment quality: Similar to Unix output with comments.

# cat Command Simulation

Simulated options for `cat` command:

- `-n` : Number all output lines, helpful for viewing or debugging files with line number reference.
- `-v` : Display non-printing characters, making control and other non-printing characters visible, except for tabs and the end of line markers.
- `-sa` : Merge input files into 1 file. It is an optimize option of SanAn Software

Comment quality: Similar to Unix output with comments.

# Evaluation and Improvements

- Current algorithm is quite simple, so there hasn't been much optimization in terms of algorithm complexity. Basic techniques such as ArrayList, HashMap have been utilized for basic programming optimizations.
- Usage of `StringBuffer`, `StringBuilder` for optimizing file read/write operations.
- Employing Streams to handle large file scenarios.
- Investigated some issues related to comments, such as the “>`” option when using the `cat` command. However, due to Unix directionality issues, appropriate handling hasn't been implemented yet. We add an optimize option like '-sa' to cover this case.

![image](https://github.com/alberttranse/java-interview/assets/15794394/520cdf61-a967-4cc9-bd5b-1db9a94ad3b2)
