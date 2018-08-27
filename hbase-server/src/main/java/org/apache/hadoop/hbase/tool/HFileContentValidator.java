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
operator|.
name|tool
package|;
end_package

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
name|Collection
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
name|concurrent
operator|.
name|ExecutorService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Executors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
name|FileSystem
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
name|HBaseInterfaceAudience
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
name|HConstants
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
name|util
operator|.
name|AbstractHBaseTool
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
name|util
operator|.
name|FSUtils
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
name|util
operator|.
name|Threads
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
name|util
operator|.
name|hbck
operator|.
name|HFileCorruptionChecker
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|CommandLine
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|TOOLS
argument_list|)
specifier|public
class|class
name|HFileContentValidator
extends|extends
name|AbstractHBaseTool
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
name|HFileContentValidator
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Check HFile contents are readable by HBase 2.    *    * @param conf used configuration    * @return number of HFiles corrupted HBase    * @throws IOException if a remote or network exception occurs    */
specifier|private
name|boolean
name|validateHFileContent
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fileSystem
init|=
name|FSUtils
operator|.
name|getCurrentFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|ExecutorService
name|threadPool
init|=
name|createThreadPool
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|HFileCorruptionChecker
name|checker
decl_stmt|;
try|try
block|{
name|checker
operator|=
operator|new
name|HFileCorruptionChecker
argument_list|(
name|conf
argument_list|,
name|threadPool
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Path
name|rootDir
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Validating HFile contents under {}"
argument_list|,
name|rootDir
argument_list|)
expr_stmt|;
name|Collection
argument_list|<
name|Path
argument_list|>
name|tableDirs
init|=
name|FSUtils
operator|.
name|getTableDirs
argument_list|(
name|fileSystem
argument_list|,
name|rootDir
argument_list|)
decl_stmt|;
name|checker
operator|.
name|checkTables
argument_list|(
name|tableDirs
argument_list|)
expr_stmt|;
name|Path
name|archiveRootDir
init|=
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
name|HConstants
operator|.
name|HFILE_ARCHIVE_DIRECTORY
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Validating HFile contents under {}"
argument_list|,
name|archiveRootDir
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|archiveTableDirs
init|=
name|FSUtils
operator|.
name|getTableDirs
argument_list|(
name|fileSystem
argument_list|,
name|archiveRootDir
argument_list|)
decl_stmt|;
name|checker
operator|.
name|checkTables
argument_list|(
name|archiveTableDirs
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|threadPool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
try|try
block|{
name|threadPool
operator|.
name|awaitTermination
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
name|int
name|checkedFiles
init|=
name|checker
operator|.
name|getHFilesChecked
argument_list|()
decl_stmt|;
name|Collection
argument_list|<
name|Path
argument_list|>
name|corrupted
init|=
name|checker
operator|.
name|getCorrupted
argument_list|()
decl_stmt|;
if|if
condition|(
name|corrupted
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Checked {} HFiles, none of them are corrupted."
argument_list|,
name|checkedFiles
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"There are no incompatible HFiles."
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Checked {} HFiles, {} are corrupted."
argument_list|,
name|checkedFiles
argument_list|,
name|corrupted
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Path
name|path
range|:
name|corrupted
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Corrupted file: {}"
argument_list|,
name|path
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Change data block encodings before upgrading. "
operator|+
literal|"Check https://s.apache.org/prefixtree for instructions."
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
specifier|private
name|ExecutorService
name|createThreadPool
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|int
name|availableProcessors
init|=
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|availableProcessors
argument_list|()
decl_stmt|;
name|int
name|numThreads
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hfilevalidator.numthreads"
argument_list|,
name|availableProcessors
argument_list|)
decl_stmt|;
return|return
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|numThreads
argument_list|,
name|Threads
operator|.
name|getNamedThreadFactory
argument_list|(
literal|"hfile-validator"
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|addOptions
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|protected
name|void
name|processOptions
parameter_list|(
name|CommandLine
name|cmd
parameter_list|)
block|{   }
annotation|@
name|Override
specifier|protected
name|int
name|doWork
parameter_list|()
throws|throws
name|Exception
block|{
return|return
operator|(
name|validateHFileContent
argument_list|(
name|getConf
argument_list|()
argument_list|)
operator|)
condition|?
name|EXIT_SUCCESS
else|:
name|EXIT_FAILURE
return|;
block|}
block|}
end_class

end_unit
