import os
import subprocess

from os import path

RUNNER_SCRIPT_BASENAME = 'run.sh'
RETURN_COMMENT_PREFIX = '// Returns:'
TEMP_OBJ_FILE_NAME = 'tmp.output.o'
DECAF_SUFFIX = '.dcf'
PROGRAMS_DIRECTORY = 'programs/'

def runner_path():
    """Get the path of the compiler's run.sh script."""
    git_base = os.popen('git rev-parse --show-toplevel').read().strip()
    return os.path.join(git_base, RUNNER_SCRIPT_BASENAME)


def without_prefix(string, prefix):
    """Return 'string' with 'prefix' chopped off the front."""
    assert string.startswith(prefix)
    return string[len(prefix):]


def expected_return_value(program_filename):
    """ Determine what return value a decaf program claims to have.

    That is, look for a comment of the form "// Returns: XXX", where
    XXX is some integer, and return that integer.
    """
    with open(program_filename) as decaf_file:
        for line in decaf_file:
            if line.startswith(RETURN_COMMENT_PREFIX):
                return_comment = line
                break
        else:
            # We didn't break-- we fell off the end of the loop.
            raise AssertionError('No "// Returns:" comment!')
    return_value = without_prefix(return_comment, RETURN_COMMENT_PREFIX)
    return int(return_value)


def assemble(assembler, out_file_name):
    """ Assemble some assembler with gcc, and store it to an output file."""
    gcc = subprocess.Popen(
        ['gcc -g -o ' + out_file_name + ' -x assembler -'], shell=True,
        stdin=subprocess.PIPE)
    gcc.communicate(assembler);
    gcc_return = gcc.wait()
    assert gcc_return == 0


def check_return_value(decaf_program):
    """Check that a decaf program has the return value it claims to have.
    
    Returns: Whether the program's return value is correct.
    """
    exp_ret_val = expected_return_value(decaf_program)

    compile_command = '%s -t assembly %s' % (runner_path(), decaf_program)
    compiler = subprocess.Popen([compile_command], shell=True,
                                stdout=subprocess.PIPE,
                                stderr=subprocess.PIPE)
    stdout, stderr =  compiler.communicate()
    compiler_return_code = compiler.wait()
    assert compiler_return_code == 0

    # TODO(jasonpr): Figure out how to use a temp file here, instead of
    # using this TEMP_OBJ_FILE_NAME string.
    assemble(stdout, TEMP_OBJ_FILE_NAME)

    runner = subprocess.Popen(['./' + TEMP_OBJ_FILE_NAME], shell=True)
    decaf_return_value = runner.wait()

    if decaf_return_value == exp_ret_val:
        print "File %s: PASS" % decaf_program
        return True
    else:
        print "File %s: FAIL (Exp: %d. Got: %d)" % (decaf_program, exp_ret_val,
                                                    decaf_return_value)
        return False

def check_programs_in_directory(directory):
    """Run check_return_value on every Decaf file in the directory.

    Returns: True if they all pass.  False if any one fails.
    """
    files = [f for f in os.listdir(directory) if f.endswith(DECAF_SUFFIX)]
    files.sort()
    files = [os.path.join(directory, f) for f in files]

    all_passed = True
    for f in files:
        if not check_return_value(f):
            all_passed = False

    return all_passed

def main():
    if check_programs_in_directory(PROGRAMS_DIRECTORY):
        # There were no errors!  Exit cleanly.
        return 0
    else:
        # There was an error.
        return 1

if __name__ == '__main__':
    exit(main())
