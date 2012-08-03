(
  cd .git
  wget -O authors.txt http://git.apache.org/authors.txt
)
git config svn.authorsfile ".git/authors.txt"
git svn init --prefix=origin/ --tags=tags --trunk=trunk --branches=branches https://svn.apache.org/repos/asf/continuum
git svn rebase
