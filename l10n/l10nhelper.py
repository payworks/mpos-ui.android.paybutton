from subprocess import call
import os


call(['l10n', 'link', '1big52YCGIDxU8Tsjh7SPjrSuwuOmuHUkMRWjup1owgA'])
call(['l10n', 'export', '--exporter=android', '--fallback', 'en_US'])

call(['cp', 'strings-en_US.xml', '../mpos-ui/src/main/res/values/strings.xml'])
call(['mv', 'strings-en_US.xml', '../mpos-ui/src/main/res/values-en/strings.xml'])
call(['mv', 'strings-de_DE.xml', '../mpos-ui/src/main/res/values-de/strings.xml'])
call(['mv', 'strings-fr_FR.xml', '../mpos-ui/src/main/res/values-fr/strings.xml'])
call(['mv', 'strings-it_IT.xml', '../mpos-ui/src/main/res/values-it/strings.xml'])
call(['mv', 'strings-pt_PT.xml', '../mpos-ui/src/main/res/values-pt/strings.xml'])
call(['mv', 'strings-es_ES.xml', '../mpos-ui/src/main/res/values-es/strings.xml'])
call(['mv', 'strings-nl_BE.xml', '../mpos-ui/src/main/res/values-nl/strings.xml'])
call(['mv', 'strings-fi_FI.xml', '../mpos-ui/src/main/res/values-fi/strings.xml'])
call(['mv', 'strings-pl_PL.xml', '../mpos-ui/src/main/res/values-pl/strings.xml'])
