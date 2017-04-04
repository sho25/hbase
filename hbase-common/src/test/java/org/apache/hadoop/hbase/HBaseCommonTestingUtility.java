begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|UUID
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
name|io
operator|.
name|FileUtils
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
name|conf
operator|.
name|Configuration
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
name|fs
operator|.
name|Path
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
comment|/**  * Common helpers for testing HBase that do not depend on specific server/etc. things.  * {@see org.apache.hadoop.hbase.HBaseTestingUtility}  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|HBaseCommonTestingUtility
block|{
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HBaseCommonTestingUtility
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|Configuration
name|conf
decl_stmt|;
specifier|public
name|HBaseCommonTestingUtility
parameter_list|()
block|{
name|this
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HBaseCommonTestingUtility
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
comment|/**    * Returns this classes's instance of {@link Configuration}.    *    * @return Instance of Configuration.    */
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|this
operator|.
name|conf
return|;
block|}
comment|/**    * System property key to get base test directory value    */
specifier|public
specifier|static
specifier|final
name|String
name|BASE_TEST_DIRECTORY_KEY
init|=
literal|"test.build.data.basedirectory"
decl_stmt|;
comment|/**    * Default base directory for test output.    */
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_BASE_TEST_DIRECTORY
init|=
literal|"target/test-data"
decl_stmt|;
comment|/**    * Directory where we put the data for this instance of HBaseTestingUtility    */
specifier|private
name|File
name|dataTestDir
init|=
literal|null
decl_stmt|;
comment|/**    * @return Where to write test data on local filesystem, specific to    * the test.  Useful for tests that do not use a cluster.    * Creates it if it does not exist already.    */
specifier|public
name|Path
name|getDataTestDir
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|dataTestDir
operator|==
literal|null
condition|)
block|{
name|setupDataTestDir
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|Path
argument_list|(
name|this
operator|.
name|dataTestDir
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @param subdirName    * @return Path to a subdirectory named<code>subdirName</code> under    * {@link #getDataTestDir()}.    * Does *NOT* create it if it does not exist.    */
specifier|public
name|Path
name|getDataTestDir
parameter_list|(
specifier|final
name|String
name|subdirName
parameter_list|)
block|{
return|return
operator|new
name|Path
argument_list|(
name|getDataTestDir
argument_list|()
argument_list|,
name|subdirName
argument_list|)
return|;
block|}
comment|/**    * Sets up a directory for a test to use.    *    * @return New directory path, if created.    */
specifier|protected
name|Path
name|setupDataTestDir
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|dataTestDir
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Data test dir already setup in "
operator|+
name|dataTestDir
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|Path
name|testPath
init|=
name|getRandomDir
argument_list|()
decl_stmt|;
name|this
operator|.
name|dataTestDir
operator|=
operator|new
name|File
argument_list|(
name|testPath
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|getAbsoluteFile
argument_list|()
expr_stmt|;
comment|// Set this property so if mapreduce jobs run, they will use this as their home dir.
name|System
operator|.
name|setProperty
argument_list|(
literal|"test.build.dir"
argument_list|,
name|this
operator|.
name|dataTestDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|deleteOnExit
argument_list|()
condition|)
name|this
operator|.
name|dataTestDir
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
name|createSubDir
argument_list|(
literal|"hbase.local.dir"
argument_list|,
name|testPath
argument_list|,
literal|"hbase-local-dir"
argument_list|)
expr_stmt|;
return|return
name|testPath
return|;
block|}
comment|/**    * @return A dir with a random (uuid) name under the test dir    * @see #getBaseTestDir()    */
specifier|public
name|Path
name|getRandomDir
parameter_list|()
block|{
return|return
operator|new
name|Path
argument_list|(
name|getBaseTestDir
argument_list|()
argument_list|,
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
specifier|protected
name|void
name|createSubDir
parameter_list|(
name|String
name|propertyName
parameter_list|,
name|Path
name|parent
parameter_list|,
name|String
name|subDirName
parameter_list|)
block|{
name|Path
name|newPath
init|=
operator|new
name|Path
argument_list|(
name|parent
argument_list|,
name|subDirName
argument_list|)
decl_stmt|;
name|File
name|newDir
init|=
operator|new
name|File
argument_list|(
name|newPath
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|getAbsoluteFile
argument_list|()
decl_stmt|;
if|if
condition|(
name|deleteOnExit
argument_list|()
condition|)
name|newDir
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|propertyName
argument_list|,
name|newDir
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return True if we should delete testing dirs on exit.    */
name|boolean
name|deleteOnExit
parameter_list|()
block|{
name|String
name|v
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"hbase.testing.preserve.testdir"
argument_list|)
decl_stmt|;
comment|// Let default be true, to delete on exit.
return|return
name|v
operator|==
literal|null
condition|?
literal|true
else|:
operator|!
name|Boolean
operator|.
name|parseBoolean
argument_list|(
name|v
argument_list|)
return|;
block|}
comment|/**    * @return True if we removed the test dirs    * @throws IOException    */
specifier|public
name|boolean
name|cleanupTestDir
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|deleteDir
argument_list|(
name|this
operator|.
name|dataTestDir
argument_list|)
condition|)
block|{
name|this
operator|.
name|dataTestDir
operator|=
literal|null
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * @param subdir Test subdir name.    * @return True if we removed the test dir    * @throws IOException    */
name|boolean
name|cleanupTestDir
parameter_list|(
specifier|final
name|String
name|subdir
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|dataTestDir
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|deleteDir
argument_list|(
operator|new
name|File
argument_list|(
name|this
operator|.
name|dataTestDir
argument_list|,
name|subdir
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * @return Where to write test data on local filesystem; usually    * {@link #DEFAULT_BASE_TEST_DIRECTORY}    * Should not be used by the unit tests, hence its's private.    * Unit test will use a subdirectory of this directory.    * @see #setupDataTestDir()    */
specifier|private
name|Path
name|getBaseTestDir
parameter_list|()
block|{
name|String
name|PathName
init|=
name|System
operator|.
name|getProperty
argument_list|(
name|BASE_TEST_DIRECTORY_KEY
argument_list|,
name|DEFAULT_BASE_TEST_DIRECTORY
argument_list|)
decl_stmt|;
return|return
operator|new
name|Path
argument_list|(
name|PathName
argument_list|)
return|;
block|}
comment|/**    * @param dir Directory to delete    * @return True if we deleted it.    * @throws IOException    */
name|boolean
name|deleteDir
parameter_list|(
specifier|final
name|File
name|dir
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|dir
operator|==
literal|null
operator|||
operator|!
name|dir
operator|.
name|exists
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
name|int
name|ntries
init|=
literal|0
decl_stmt|;
do|do
block|{
name|ntries
operator|+=
literal|1
expr_stmt|;
try|try
block|{
if|if
condition|(
name|deleteOnExit
argument_list|()
condition|)
name|FileUtils
operator|.
name|deleteDirectory
argument_list|(
name|dir
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to delete "
operator|+
name|dir
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to delete "
operator|+
name|dir
operator|.
name|getAbsolutePath
argument_list|()
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
do|while
condition|(
name|ntries
operator|<
literal|30
condition|)
do|;
return|return
name|ntries
operator|<
literal|30
return|;
block|}
block|}
end_class

end_unit

