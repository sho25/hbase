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
operator|.
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintWriter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|Version
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * This class finds the Version information for HBase.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|VersionInfo
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|VersionInfo
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|// If between two dots there is not a number, we regard it as a very large number so it is
comment|// higher than any numbers in the version.
specifier|private
specifier|static
name|int
name|VERY_LARGE_NUMBER
init|=
literal|100000
decl_stmt|;
comment|/**    * Get the hbase version.    * @return the hbase version string, eg. "0.6.3-dev"    */
specifier|public
specifier|static
name|String
name|getVersion
parameter_list|()
block|{
return|return
name|Version
operator|.
name|version
return|;
block|}
comment|/**    * Get the subversion revision number for the root directory    * @return the revision number, eg. "451451"    */
specifier|public
specifier|static
name|String
name|getRevision
parameter_list|()
block|{
return|return
name|Version
operator|.
name|revision
return|;
block|}
comment|/**    * The date that hbase was compiled.    * @return the compilation date in unix date format    */
specifier|public
specifier|static
name|String
name|getDate
parameter_list|()
block|{
return|return
name|Version
operator|.
name|date
return|;
block|}
comment|/**    * The user that compiled hbase.    * @return the username of the user    */
specifier|public
specifier|static
name|String
name|getUser
parameter_list|()
block|{
return|return
name|Version
operator|.
name|user
return|;
block|}
comment|/**    * Get the subversion URL for the root hbase directory.    * @return the url    */
specifier|public
specifier|static
name|String
name|getUrl
parameter_list|()
block|{
return|return
name|Version
operator|.
name|url
return|;
block|}
specifier|static
name|String
index|[]
name|versionReport
parameter_list|()
block|{
return|return
operator|new
name|String
index|[]
block|{
literal|"HBase "
operator|+
name|getVersion
argument_list|()
block|,
literal|"Source code repository "
operator|+
name|getUrl
argument_list|()
operator|+
literal|" revision="
operator|+
name|getRevision
argument_list|()
block|,
literal|"Compiled by "
operator|+
name|getUser
argument_list|()
operator|+
literal|" on "
operator|+
name|getDate
argument_list|()
block|,
literal|"From source with checksum "
operator|+
name|getSrcChecksum
argument_list|()
block|}
return|;
block|}
comment|/**    * Get the checksum of the source files from which Hadoop was compiled.    * @return a string that uniquely identifies the source    **/
specifier|public
specifier|static
name|String
name|getSrcChecksum
parameter_list|()
block|{
return|return
name|Version
operator|.
name|srcChecksum
return|;
block|}
specifier|public
specifier|static
name|void
name|writeTo
parameter_list|(
name|PrintWriter
name|out
parameter_list|)
block|{
for|for
control|(
name|String
name|line
range|:
name|versionReport
argument_list|()
control|)
block|{
name|out
operator|.
name|println
argument_list|(
name|line
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|writeTo
parameter_list|(
name|PrintStream
name|out
parameter_list|)
block|{
for|for
control|(
name|String
name|line
range|:
name|versionReport
argument_list|()
control|)
block|{
name|out
operator|.
name|println
argument_list|(
name|line
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|logVersion
parameter_list|()
block|{
for|for
control|(
name|String
name|line
range|:
name|versionReport
argument_list|()
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|line
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|int
name|compareVersion
parameter_list|(
name|String
name|v1
parameter_list|,
name|String
name|v2
parameter_list|)
block|{
comment|//fast compare equals first
if|if
condition|(
name|v1
operator|.
name|equals
argument_list|(
name|v2
argument_list|)
condition|)
block|{
return|return
literal|0
return|;
block|}
name|String
name|s1
index|[]
init|=
name|v1
operator|.
name|split
argument_list|(
literal|"\\.|-"
argument_list|)
decl_stmt|;
comment|//1.2.3-hotfix -> [1, 2, 3, hotfix]
name|String
name|s2
index|[]
init|=
name|v2
operator|.
name|split
argument_list|(
literal|"\\.|-"
argument_list|)
decl_stmt|;
name|int
name|index
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|index
operator|<
name|s1
operator|.
name|length
operator|&&
name|index
operator|<
name|s2
operator|.
name|length
condition|)
block|{
name|int
name|va
init|=
name|VERY_LARGE_NUMBER
decl_stmt|,
name|vb
init|=
name|VERY_LARGE_NUMBER
decl_stmt|;
try|try
block|{
name|va
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|s1
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ingore
parameter_list|)
block|{       }
try|try
block|{
name|vb
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|s2
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ingore
parameter_list|)
block|{       }
if|if
condition|(
name|va
operator|!=
name|vb
condition|)
block|{
return|return
name|va
operator|-
name|vb
return|;
block|}
if|if
condition|(
name|va
operator|==
name|VERY_LARGE_NUMBER
condition|)
block|{
comment|// compare as String
name|int
name|c
init|=
name|s1
index|[
name|index
index|]
operator|.
name|compareTo
argument_list|(
name|s2
index|[
name|index
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|c
operator|!=
literal|0
condition|)
block|{
return|return
name|c
return|;
block|}
block|}
name|index
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|index
operator|<
name|s1
operator|.
name|length
condition|)
block|{
comment|// s1 is longer
return|return
literal|1
return|;
block|}
comment|//s2 is longer
return|return
operator|-
literal|1
return|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
name|writeTo
argument_list|(
name|System
operator|.
name|out
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

