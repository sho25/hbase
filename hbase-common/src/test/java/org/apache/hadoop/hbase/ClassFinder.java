begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileFilter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Enumeration
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|jar
operator|.
name|JarEntry
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|jar
operator|.
name|JarInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * A class that finds a set of classes that are locally accessible  * (from .class or .jar files), and satisfy the conditions that are  * imposed by name and class filters provided by the user.  */
end_comment

begin_class
specifier|public
class|class
name|ClassFinder
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ClassFinder
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|String
name|CLASS_EXT
init|=
literal|".class"
decl_stmt|;
specifier|private
name|ResourcePathFilter
name|resourcePathFilter
decl_stmt|;
specifier|private
name|FileNameFilter
name|fileNameFilter
decl_stmt|;
specifier|private
name|ClassFilter
name|classFilter
decl_stmt|;
specifier|private
name|FileFilter
name|fileFilter
decl_stmt|;
specifier|private
name|ClassLoader
name|classLoader
decl_stmt|;
specifier|public
interface|interface
name|ResourcePathFilter
block|{
name|boolean
name|isCandidatePath
parameter_list|(
name|String
name|resourcePath
parameter_list|,
name|boolean
name|isJar
parameter_list|)
function_decl|;
block|}
specifier|public
interface|interface
name|FileNameFilter
block|{
name|boolean
name|isCandidateFile
parameter_list|(
name|String
name|fileName
parameter_list|,
name|String
name|absFilePath
parameter_list|)
function_decl|;
block|}
specifier|public
interface|interface
name|ClassFilter
block|{
name|boolean
name|isCandidateClass
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|c
parameter_list|)
function_decl|;
block|}
specifier|public
specifier|static
class|class
name|Not
implements|implements
name|ResourcePathFilter
implements|,
name|FileNameFilter
implements|,
name|ClassFilter
block|{
specifier|private
name|ResourcePathFilter
name|resourcePathFilter
decl_stmt|;
specifier|private
name|FileNameFilter
name|fileNameFilter
decl_stmt|;
specifier|private
name|ClassFilter
name|classFilter
decl_stmt|;
specifier|public
name|Not
parameter_list|(
name|ResourcePathFilter
name|resourcePathFilter
parameter_list|)
block|{
name|this
operator|.
name|resourcePathFilter
operator|=
name|resourcePathFilter
expr_stmt|;
block|}
specifier|public
name|Not
parameter_list|(
name|FileNameFilter
name|fileNameFilter
parameter_list|)
block|{
name|this
operator|.
name|fileNameFilter
operator|=
name|fileNameFilter
expr_stmt|;
block|}
specifier|public
name|Not
parameter_list|(
name|ClassFilter
name|classFilter
parameter_list|)
block|{
name|this
operator|.
name|classFilter
operator|=
name|classFilter
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isCandidatePath
parameter_list|(
name|String
name|resourcePath
parameter_list|,
name|boolean
name|isJar
parameter_list|)
block|{
return|return
operator|!
name|resourcePathFilter
operator|.
name|isCandidatePath
argument_list|(
name|resourcePath
argument_list|,
name|isJar
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isCandidateFile
parameter_list|(
name|String
name|fileName
parameter_list|,
name|String
name|absFilePath
parameter_list|)
block|{
return|return
operator|!
name|fileNameFilter
operator|.
name|isCandidateFile
argument_list|(
name|fileName
argument_list|,
name|absFilePath
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isCandidateClass
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|c
parameter_list|)
block|{
return|return
operator|!
name|classFilter
operator|.
name|isCandidateClass
argument_list|(
name|c
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|And
implements|implements
name|ClassFilter
implements|,
name|ResourcePathFilter
block|{
name|ClassFilter
index|[]
name|classFilters
decl_stmt|;
name|ResourcePathFilter
index|[]
name|resourcePathFilters
decl_stmt|;
specifier|public
name|And
parameter_list|(
name|ClassFilter
modifier|...
name|classFilters
parameter_list|)
block|{
name|this
operator|.
name|classFilters
operator|=
name|classFilters
expr_stmt|;
block|}
specifier|public
name|And
parameter_list|(
name|ResourcePathFilter
modifier|...
name|resourcePathFilters
parameter_list|)
block|{
name|this
operator|.
name|resourcePathFilters
operator|=
name|resourcePathFilters
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isCandidateClass
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|c
parameter_list|)
block|{
for|for
control|(
name|ClassFilter
name|filter
range|:
name|classFilters
control|)
block|{
if|if
condition|(
operator|!
name|filter
operator|.
name|isCandidateClass
argument_list|(
name|c
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isCandidatePath
parameter_list|(
name|String
name|resourcePath
parameter_list|,
name|boolean
name|isJar
parameter_list|)
block|{
for|for
control|(
name|ResourcePathFilter
name|filter
range|:
name|resourcePathFilters
control|)
block|{
if|if
condition|(
operator|!
name|filter
operator|.
name|isCandidatePath
argument_list|(
name|resourcePath
argument_list|,
name|isJar
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
block|}
comment|// To control which classloader to use while trying to find jars/classes
specifier|public
name|ClassFinder
parameter_list|(
name|ClassLoader
name|classLoader
parameter_list|)
block|{
name|this
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|classLoader
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ClassFinder
parameter_list|(
name|ResourcePathFilter
name|resourcePathFilter
parameter_list|,
name|FileNameFilter
name|fileNameFilter
parameter_list|,
name|ClassFilter
name|classFilter
parameter_list|)
block|{
name|this
argument_list|(
name|resourcePathFilter
argument_list|,
name|fileNameFilter
argument_list|,
name|classFilter
argument_list|,
name|ClassLoader
operator|.
name|getSystemClassLoader
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ClassFinder
parameter_list|(
name|ResourcePathFilter
name|resourcePathFilter
parameter_list|,
name|FileNameFilter
name|fileNameFilter
parameter_list|,
name|ClassFilter
name|classFilter
parameter_list|,
name|ClassLoader
name|classLoader
parameter_list|)
block|{
name|this
operator|.
name|resourcePathFilter
operator|=
name|resourcePathFilter
expr_stmt|;
name|this
operator|.
name|classFilter
operator|=
name|classFilter
expr_stmt|;
name|this
operator|.
name|fileNameFilter
operator|=
name|fileNameFilter
expr_stmt|;
name|this
operator|.
name|fileFilter
operator|=
operator|new
name|FileFilterWithName
argument_list|(
name|fileNameFilter
argument_list|)
expr_stmt|;
name|this
operator|.
name|classLoader
operator|=
name|classLoader
expr_stmt|;
block|}
comment|/**    * Finds the classes in current package (of ClassFinder) and nested packages.    * @param proceedOnExceptions whether to ignore exceptions encountered for    *        individual jars/files/classes, and proceed looking for others.    */
specifier|public
name|Set
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|findClasses
parameter_list|(
name|boolean
name|proceedOnExceptions
parameter_list|)
throws|throws
name|ClassNotFoundException
throws|,
name|IOException
throws|,
name|LinkageError
block|{
return|return
name|findClasses
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getPackage
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|proceedOnExceptions
argument_list|)
return|;
block|}
comment|/**    * Finds the classes in a package and nested packages.    * @param packageName package names    * @param proceedOnExceptions whether to ignore exceptions encountered for    *        individual jars/files/classes, and proceed looking for others.    */
specifier|public
name|Set
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|findClasses
parameter_list|(
name|String
name|packageName
parameter_list|,
name|boolean
name|proceedOnExceptions
parameter_list|)
throws|throws
name|ClassNotFoundException
throws|,
name|IOException
throws|,
name|LinkageError
block|{
specifier|final
name|String
name|path
init|=
name|packageName
operator|.
name|replace
argument_list|(
literal|'.'
argument_list|,
literal|'/'
argument_list|)
decl_stmt|;
specifier|final
name|Pattern
name|jarResourceRe
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"^file:(.+\\.jar)!/"
operator|+
name|path
operator|+
literal|"$"
argument_list|)
decl_stmt|;
name|Enumeration
argument_list|<
name|URL
argument_list|>
name|resources
init|=
name|this
operator|.
name|classLoader
operator|.
name|getResources
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|File
argument_list|>
name|dirs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|jars
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
while|while
condition|(
name|resources
operator|.
name|hasMoreElements
argument_list|()
condition|)
block|{
name|URL
name|resource
init|=
name|resources
operator|.
name|nextElement
argument_list|()
decl_stmt|;
name|String
name|resourcePath
init|=
name|resource
operator|.
name|getFile
argument_list|()
decl_stmt|;
name|Matcher
name|matcher
init|=
name|jarResourceRe
operator|.
name|matcher
argument_list|(
name|resourcePath
argument_list|)
decl_stmt|;
name|boolean
name|isJar
init|=
name|matcher
operator|.
name|find
argument_list|()
decl_stmt|;
name|resourcePath
operator|=
name|isJar
condition|?
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
else|:
name|resourcePath
expr_stmt|;
if|if
condition|(
literal|null
operator|==
name|this
operator|.
name|resourcePathFilter
operator|||
name|this
operator|.
name|resourcePathFilter
operator|.
name|isCandidatePath
argument_list|(
name|resourcePath
argument_list|,
name|isJar
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Looking in "
operator|+
name|resourcePath
operator|+
literal|"; isJar="
operator|+
name|isJar
argument_list|)
expr_stmt|;
if|if
condition|(
name|isJar
condition|)
block|{
name|jars
operator|.
name|add
argument_list|(
name|resourcePath
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|dirs
operator|.
name|add
argument_list|(
operator|new
name|File
argument_list|(
name|resourcePath
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|Set
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|classes
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|File
name|directory
range|:
name|dirs
control|)
block|{
name|classes
operator|.
name|addAll
argument_list|(
name|findClassesFromFiles
argument_list|(
name|directory
argument_list|,
name|packageName
argument_list|,
name|proceedOnExceptions
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|jarFileName
range|:
name|jars
control|)
block|{
name|classes
operator|.
name|addAll
argument_list|(
name|findClassesFromJar
argument_list|(
name|jarFileName
argument_list|,
name|packageName
argument_list|,
name|proceedOnExceptions
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|classes
return|;
block|}
specifier|private
name|Set
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|findClassesFromJar
parameter_list|(
name|String
name|jarFileName
parameter_list|,
name|String
name|packageName
parameter_list|,
name|boolean
name|proceedOnExceptions
parameter_list|)
throws|throws
name|IOException
throws|,
name|ClassNotFoundException
throws|,
name|LinkageError
block|{
name|JarInputStream
name|jarFile
decl_stmt|;
try|try
block|{
name|jarFile
operator|=
operator|new
name|JarInputStream
argument_list|(
operator|new
name|FileInputStream
argument_list|(
name|jarFileName
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioEx
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to look for classes in "
operator|+
name|jarFileName
operator|+
literal|": "
operator|+
name|ioEx
argument_list|)
expr_stmt|;
throw|throw
name|ioEx
throw|;
block|}
name|Set
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|classes
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|JarEntry
name|entry
decl_stmt|;
try|try
block|{
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
name|entry
operator|=
name|jarFile
operator|.
name|getNextJarEntry
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioEx
parameter_list|)
block|{
if|if
condition|(
operator|!
name|proceedOnExceptions
condition|)
block|{
throw|throw
name|ioEx
throw|;
block|}
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to get next entry from "
operator|+
name|jarFileName
operator|+
literal|": "
operator|+
name|ioEx
argument_list|)
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|entry
operator|==
literal|null
condition|)
block|{
break|break;
comment|// loop termination condition
block|}
name|String
name|className
init|=
name|entry
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|className
operator|.
name|endsWith
argument_list|(
name|CLASS_EXT
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|int
name|ix
init|=
name|className
operator|.
name|lastIndexOf
argument_list|(
literal|'/'
argument_list|)
decl_stmt|;
name|String
name|fileName
init|=
operator|(
name|ix
operator|>=
literal|0
operator|)
condition|?
name|className
operator|.
name|substring
argument_list|(
name|ix
operator|+
literal|1
argument_list|)
else|:
name|className
decl_stmt|;
if|if
condition|(
literal|null
operator|!=
name|this
operator|.
name|fileNameFilter
operator|&&
operator|!
name|this
operator|.
name|fileNameFilter
operator|.
name|isCandidateFile
argument_list|(
name|fileName
argument_list|,
name|className
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|className
operator|=
name|className
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|className
operator|.
name|length
argument_list|()
operator|-
name|CLASS_EXT
operator|.
name|length
argument_list|()
argument_list|)
operator|.
name|replace
argument_list|(
literal|'/'
argument_list|,
literal|'.'
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|className
operator|.
name|startsWith
argument_list|(
name|packageName
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|Class
argument_list|<
name|?
argument_list|>
name|c
init|=
name|makeClass
argument_list|(
name|className
argument_list|,
name|proceedOnExceptions
argument_list|)
decl_stmt|;
if|if
condition|(
name|c
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|classes
operator|.
name|add
argument_list|(
name|c
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Ignoring duplicate class "
operator|+
name|className
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|classes
return|;
block|}
finally|finally
block|{
name|jarFile
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|Set
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|findClassesFromFiles
parameter_list|(
name|File
name|baseDirectory
parameter_list|,
name|String
name|packageName
parameter_list|,
name|boolean
name|proceedOnExceptions
parameter_list|)
throws|throws
name|ClassNotFoundException
throws|,
name|LinkageError
block|{
name|Set
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|classes
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|baseDirectory
operator|.
name|exists
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|baseDirectory
operator|.
name|getAbsolutePath
argument_list|()
operator|+
literal|" does not exist"
argument_list|)
expr_stmt|;
return|return
name|classes
return|;
block|}
name|File
index|[]
name|files
init|=
name|baseDirectory
operator|.
name|listFiles
argument_list|(
name|this
operator|.
name|fileFilter
argument_list|)
decl_stmt|;
if|if
condition|(
name|files
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to get files from "
operator|+
name|baseDirectory
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|classes
return|;
block|}
for|for
control|(
name|File
name|file
range|:
name|files
control|)
block|{
specifier|final
name|String
name|fileName
init|=
name|file
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|file
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
name|classes
operator|.
name|addAll
argument_list|(
name|findClassesFromFiles
argument_list|(
name|file
argument_list|,
name|packageName
operator|+
literal|"."
operator|+
name|fileName
argument_list|,
name|proceedOnExceptions
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|String
name|className
init|=
name|packageName
operator|+
literal|'.'
operator|+
name|fileName
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|fileName
operator|.
name|length
argument_list|()
operator|-
name|CLASS_EXT
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
name|c
init|=
name|makeClass
argument_list|(
name|className
argument_list|,
name|proceedOnExceptions
argument_list|)
decl_stmt|;
if|if
condition|(
name|c
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|classes
operator|.
name|add
argument_list|(
name|c
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Ignoring duplicate class "
operator|+
name|className
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|classes
return|;
block|}
specifier|private
name|Class
argument_list|<
name|?
argument_list|>
name|makeClass
parameter_list|(
name|String
name|className
parameter_list|,
name|boolean
name|proceedOnExceptions
parameter_list|)
throws|throws
name|ClassNotFoundException
throws|,
name|LinkageError
block|{
try|try
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|c
init|=
name|Class
operator|.
name|forName
argument_list|(
name|className
argument_list|,
literal|false
argument_list|,
name|classLoader
argument_list|)
decl_stmt|;
name|boolean
name|isCandidateClass
init|=
literal|null
operator|==
name|classFilter
operator|||
name|classFilter
operator|.
name|isCandidateClass
argument_list|(
name|c
argument_list|)
decl_stmt|;
return|return
name|isCandidateClass
condition|?
name|c
else|:
literal|null
return|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
decl||
name|LinkageError
name|exception
parameter_list|)
block|{
if|if
condition|(
operator|!
name|proceedOnExceptions
condition|)
block|{
throw|throw
name|exception
throw|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Failed to instantiate or check "
operator|+
name|className
operator|+
literal|": "
operator|+
name|exception
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
specifier|static
class|class
name|FileFilterWithName
implements|implements
name|FileFilter
block|{
specifier|private
name|FileNameFilter
name|nameFilter
decl_stmt|;
specifier|public
name|FileFilterWithName
parameter_list|(
name|FileNameFilter
name|nameFilter
parameter_list|)
block|{
name|this
operator|.
name|nameFilter
operator|=
name|nameFilter
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|File
name|file
parameter_list|)
block|{
return|return
name|file
operator|.
name|isDirectory
argument_list|()
operator|||
operator|(
name|file
operator|.
name|getName
argument_list|()
operator|.
name|endsWith
argument_list|(
name|CLASS_EXT
argument_list|)
operator|&&
operator|(
literal|null
operator|==
name|nameFilter
operator|||
name|nameFilter
operator|.
name|isCandidateFile
argument_list|(
name|file
operator|.
name|getName
argument_list|()
argument_list|,
name|file
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
operator|)
operator|)
return|;
block|}
block|}
block|}
end_class

end_unit

