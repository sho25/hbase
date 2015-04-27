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
name|IOException
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|concurrent
operator|.
name|Callable
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
name|ConcurrentHashMap
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
name|ExecutionException
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
name|Future
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
name|cli
operator|.
name|CommandLine
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
name|cli
operator|.
name|CommandLineParser
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
name|cli
operator|.
name|GnuParser
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
name|cli
operator|.
name|HelpFormatter
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
name|cli
operator|.
name|Option
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
name|cli
operator|.
name|Options
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
name|cli
operator|.
name|ParseException
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
name|Configured
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
name|FSDataInputStream
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
name|FileStatus
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
name|HBaseConfiguration
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
name|NamespaceDescriptor
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
name|io
operator|.
name|FileLink
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
name|io
operator|.
name|HFileLink
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
name|regionserver
operator|.
name|HRegionFileSystem
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
name|regionserver
operator|.
name|StoreFileInfo
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
name|util
operator|.
name|Tool
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
name|util
operator|.
name|ToolRunner
import|;
end_import

begin_comment
comment|/**  * Tool to detect presence of any HFileV1 in the given directory. It prints all such regions which  * have such files.  *<p>  * To print the help section of the tool:  *</p>  *<ul>  *<li>./bin/hbase org.apache.hadoop.hbase.util.HFileV1Detector --h or,</li>  *<li>java -cp `hbase classpath` org.apache.hadoop.hbase.util.HFileV1Detector --h</li>  *</ul>  *<p>  * It also supports -h, --help, -help options.  *</p>  */
end_comment

begin_class
specifier|public
class|class
name|HFileV1Detector
extends|extends
name|Configured
implements|implements
name|Tool
block|{
specifier|private
name|FileSystem
name|fs
decl_stmt|;
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
name|HFileV1Detector
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_NUM_OF_THREADS
init|=
literal|10
decl_stmt|;
comment|/**    * Pre-namespace archive directory    */
specifier|private
specifier|static
specifier|final
name|String
name|PRE_NS_DOT_ARCHIVE
init|=
literal|".archive"
decl_stmt|;
comment|/**    * Pre-namespace tmp directory    */
specifier|private
specifier|static
specifier|final
name|String
name|PRE_NS_DOT_TMP
init|=
literal|".tmp"
decl_stmt|;
specifier|private
name|int
name|numOfThreads
decl_stmt|;
comment|/**    * directory to start the processing.    */
specifier|private
name|Path
name|targetDirPath
decl_stmt|;
comment|/**    * executor for processing regions.    */
specifier|private
name|ExecutorService
name|exec
decl_stmt|;
comment|/**    * Keeps record of processed tables.    */
specifier|private
specifier|final
name|Set
argument_list|<
name|Path
argument_list|>
name|processedTables
init|=
operator|new
name|HashSet
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * set of corrupted HFiles (with undetermined major version)    */
specifier|private
specifier|final
name|Set
argument_list|<
name|Path
argument_list|>
name|corruptedHFiles
init|=
name|Collections
operator|.
name|newSetFromMap
argument_list|(
operator|new
name|ConcurrentHashMap
argument_list|<
name|Path
argument_list|,
name|Boolean
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
comment|/**    * set of HfileV1;    */
specifier|private
specifier|final
name|Set
argument_list|<
name|Path
argument_list|>
name|hFileV1Set
init|=
name|Collections
operator|.
name|newSetFromMap
argument_list|(
operator|new
name|ConcurrentHashMap
argument_list|<
name|Path
argument_list|,
name|Boolean
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|Options
name|options
init|=
operator|new
name|Options
argument_list|()
decl_stmt|;
comment|/**    * used for computing pre-namespace paths for hfilelinks    */
specifier|private
name|Path
name|defaultNamespace
decl_stmt|;
specifier|public
name|HFileV1Detector
parameter_list|()
block|{
name|Option
name|pathOption
init|=
operator|new
name|Option
argument_list|(
literal|"p"
argument_list|,
literal|"path"
argument_list|,
literal|true
argument_list|,
literal|"Path to a table, or hbase installation"
argument_list|)
decl_stmt|;
name|pathOption
operator|.
name|setRequired
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|pathOption
argument_list|)
expr_stmt|;
name|Option
name|threadOption
init|=
operator|new
name|Option
argument_list|(
literal|"n"
argument_list|,
literal|"numberOfThreads"
argument_list|,
literal|true
argument_list|,
literal|"Number of threads to use while processing HFiles."
argument_list|)
decl_stmt|;
name|threadOption
operator|.
name|setRequired
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|threadOption
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|"h"
argument_list|,
literal|"help"
argument_list|,
literal|false
argument_list|,
literal|"Help"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|parseOption
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|ParseException
throws|,
name|IOException
block|{
if|if
condition|(
name|args
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
literal|true
return|;
comment|// no args will process with default values.
block|}
name|CommandLineParser
name|parser
init|=
operator|new
name|GnuParser
argument_list|()
decl_stmt|;
name|CommandLine
name|cmd
init|=
name|parser
operator|.
name|parse
argument_list|(
name|options
argument_list|,
name|args
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"h"
argument_list|)
condition|)
block|{
name|HelpFormatter
name|formatter
init|=
operator|new
name|HelpFormatter
argument_list|()
decl_stmt|;
name|formatter
operator|.
name|printHelp
argument_list|(
literal|"HFileV1Detector"
argument_list|,
name|options
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"In case no option is provided, it processes hbase.rootdir using 10 threads."
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Example:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|" To detect any HFileV1 in a given hbase installation '/myhbase':"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|" $ $HBASE_HOME/bin/hbase "
operator|+
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" -p /myhbase"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"p"
argument_list|)
condition|)
block|{
name|this
operator|.
name|targetDirPath
operator|=
operator|new
name|Path
argument_list|(
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|getConf
argument_list|()
argument_list|)
argument_list|,
name|cmd
operator|.
name|getOptionValue
argument_list|(
literal|"p"
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"n"
argument_list|)
condition|)
block|{
name|int
name|n
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
literal|"n"
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|n
argument_list|<
literal|0
operator|||
name|n
argument_list|>
literal|100
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Please use a positive number<= 100 for number of threads."
operator|+
literal|" Continuing with default value "
operator|+
name|DEFAULT_NUM_OF_THREADS
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
name|this
operator|.
name|numOfThreads
operator|=
name|n
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|nfe
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Please select a valid number for threads"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
comment|/**    * Checks for HFileV1.    * @return 0 when no HFileV1 is present.    *         1 when a HFileV1 is present or, when there is a file with corrupt major version    *          (neither V1 nor V2).    *        -1 in case of any error/exception    */
annotation|@
name|Override
specifier|public
name|int
name|run
parameter_list|(
name|String
name|args
index|[]
parameter_list|)
throws|throws
name|IOException
throws|,
name|ParseException
block|{
name|FSUtils
operator|.
name|setFsDefault
argument_list|(
name|getConf
argument_list|()
argument_list|,
operator|new
name|Path
argument_list|(
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|getConf
argument_list|()
argument_list|)
operator|.
name|toUri
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
name|numOfThreads
operator|=
name|DEFAULT_NUM_OF_THREADS
expr_stmt|;
name|targetDirPath
operator|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|parseOption
argument_list|(
name|args
argument_list|)
condition|)
block|{
name|System
operator|.
name|exit
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|exec
operator|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|numOfThreads
argument_list|)
expr_stmt|;
try|try
block|{
return|return
name|processResult
argument_list|(
name|checkForV1Files
argument_list|(
name|targetDirPath
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|exec
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|fs
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
operator|-
literal|1
return|;
block|}
specifier|private
name|void
name|setDefaultNamespaceDir
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|dataDir
init|=
operator|new
name|Path
argument_list|(
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|getConf
argument_list|()
argument_list|)
argument_list|,
name|HConstants
operator|.
name|BASE_NAMESPACE_DIR
argument_list|)
decl_stmt|;
name|defaultNamespace
operator|=
operator|new
name|Path
argument_list|(
name|dataDir
argument_list|,
name|NamespaceDescriptor
operator|.
name|DEFAULT_NAMESPACE_NAME_STR
argument_list|)
expr_stmt|;
block|}
specifier|private
name|int
name|processResult
parameter_list|(
name|Set
argument_list|<
name|Path
argument_list|>
name|regionsWithHFileV1
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Result: \n"
argument_list|)
expr_stmt|;
name|printSet
argument_list|(
name|processedTables
argument_list|,
literal|"Tables Processed: "
argument_list|)
expr_stmt|;
name|int
name|count
init|=
name|hFileV1Set
operator|.
name|size
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Count of HFileV1: "
operator|+
name|count
argument_list|)
expr_stmt|;
if|if
condition|(
name|count
operator|>
literal|0
condition|)
name|printSet
argument_list|(
name|hFileV1Set
argument_list|,
literal|"HFileV1:"
argument_list|)
expr_stmt|;
name|count
operator|=
name|corruptedHFiles
operator|.
name|size
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Count of corrupted files: "
operator|+
name|count
argument_list|)
expr_stmt|;
if|if
condition|(
name|count
operator|>
literal|0
condition|)
name|printSet
argument_list|(
name|corruptedHFiles
argument_list|,
literal|"Corrupted Files: "
argument_list|)
expr_stmt|;
name|count
operator|=
name|regionsWithHFileV1
operator|.
name|size
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Count of Regions with HFileV1: "
operator|+
name|count
argument_list|)
expr_stmt|;
if|if
condition|(
name|count
operator|>
literal|0
condition|)
name|printSet
argument_list|(
name|regionsWithHFileV1
argument_list|,
literal|"Regions to Major Compact: "
argument_list|)
expr_stmt|;
return|return
operator|(
name|hFileV1Set
operator|.
name|isEmpty
argument_list|()
operator|&&
name|corruptedHFiles
operator|.
name|isEmpty
argument_list|()
operator|)
condition|?
literal|0
else|:
literal|1
return|;
block|}
specifier|private
name|void
name|printSet
parameter_list|(
name|Set
argument_list|<
name|Path
argument_list|>
name|result
parameter_list|,
name|String
name|msg
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|msg
argument_list|)
expr_stmt|;
for|for
control|(
name|Path
name|p
range|:
name|result
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Takes a directory path, and lists out any HFileV1, if present.    * @param targetDir directory to start looking for HFilev1.    * @return set of Regions that have HFileV1    * @throws IOException    */
specifier|private
name|Set
argument_list|<
name|Path
argument_list|>
name|checkForV1Files
parameter_list|(
name|Path
name|targetDir
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Target dir is: "
operator|+
name|targetDir
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|targetDir
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"The given path does not exist: "
operator|+
name|targetDir
argument_list|)
throw|;
block|}
if|if
condition|(
name|isTableDir
argument_list|(
name|fs
argument_list|,
name|targetDir
argument_list|)
condition|)
block|{
name|processedTables
operator|.
name|add
argument_list|(
name|targetDir
argument_list|)
expr_stmt|;
return|return
name|processTable
argument_list|(
name|targetDir
argument_list|)
return|;
block|}
name|Set
argument_list|<
name|Path
argument_list|>
name|regionsWithHFileV1
init|=
operator|new
name|HashSet
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
name|FileStatus
index|[]
name|fsStats
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|targetDir
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|fsStat
range|:
name|fsStats
control|)
block|{
if|if
condition|(
name|isTableDir
argument_list|(
name|fs
argument_list|,
name|fsStat
operator|.
name|getPath
argument_list|()
argument_list|)
operator|&&
operator|!
name|isRootTable
argument_list|(
name|fsStat
operator|.
name|getPath
argument_list|()
argument_list|)
condition|)
block|{
name|processedTables
operator|.
name|add
argument_list|(
name|fsStat
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
comment|// look for regions and find out any v1 file.
name|regionsWithHFileV1
operator|.
name|addAll
argument_list|(
name|processTable
argument_list|(
name|fsStat
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Ignoring path: "
operator|+
name|fsStat
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|regionsWithHFileV1
return|;
block|}
comment|/**    * Ignore ROOT table as it doesn't exist in 0.96.    * @param path    */
specifier|private
name|boolean
name|isRootTable
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
if|if
condition|(
name|path
operator|!=
literal|null
operator|&&
name|path
operator|.
name|toString
argument_list|()
operator|.
name|endsWith
argument_list|(
literal|"-ROOT-"
argument_list|)
condition|)
return|return
literal|true
return|;
return|return
literal|false
return|;
block|}
comment|/**    * Find out regions in the table which have HFileV1.    * @param tableDir    * @return the set of regions containing HFile v1.    * @throws IOException    */
specifier|private
name|Set
argument_list|<
name|Path
argument_list|>
name|processTable
parameter_list|(
name|Path
name|tableDir
parameter_list|)
throws|throws
name|IOException
block|{
comment|// list out the regions and then process each file in it.
name|LOG
operator|.
name|debug
argument_list|(
literal|"processing table: "
operator|+
name|tableDir
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Future
argument_list|<
name|Path
argument_list|>
argument_list|>
name|regionLevelResults
init|=
operator|new
name|ArrayList
argument_list|<
name|Future
argument_list|<
name|Path
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|Path
argument_list|>
name|regionsWithHFileV1
init|=
operator|new
name|HashSet
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
name|FileStatus
index|[]
name|fsStats
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|tableDir
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|fsStat
range|:
name|fsStats
control|)
block|{
comment|// process each region
if|if
condition|(
name|isRegionDir
argument_list|(
name|fs
argument_list|,
name|fsStat
operator|.
name|getPath
argument_list|()
argument_list|)
condition|)
block|{
name|regionLevelResults
operator|.
name|add
argument_list|(
name|processRegion
argument_list|(
name|fsStat
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|Future
argument_list|<
name|Path
argument_list|>
name|f
range|:
name|regionLevelResults
control|)
block|{
try|try
block|{
if|if
condition|(
name|f
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|regionsWithHFileV1
operator|.
name|add
argument_list|(
name|f
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
argument_list|)
expr_stmt|;
comment|// might be a bad hfile. We print it at the end.
block|}
block|}
return|return
name|regionsWithHFileV1
return|;
block|}
comment|/**    * Each region is processed by a separate handler. If a HRegion has a hfileV1, its path is    * returned as the future result, otherwise, a null value is returned.    * @param regionDir Region to process.    * @return corresponding Future object.    */
specifier|private
name|Future
argument_list|<
name|Path
argument_list|>
name|processRegion
parameter_list|(
specifier|final
name|Path
name|regionDir
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"processing region: "
operator|+
name|regionDir
argument_list|)
expr_stmt|;
name|Callable
argument_list|<
name|Path
argument_list|>
name|regionCallable
init|=
operator|new
name|Callable
argument_list|<
name|Path
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Path
name|call
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|Path
name|familyDir
range|:
name|FSUtils
operator|.
name|getFamilyDirs
argument_list|(
name|fs
argument_list|,
name|regionDir
argument_list|)
control|)
block|{
name|FileStatus
index|[]
name|storeFiles
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|familyDir
argument_list|)
decl_stmt|;
if|if
condition|(
name|storeFiles
operator|==
literal|null
operator|||
name|storeFiles
operator|.
name|length
operator|==
literal|0
condition|)
continue|continue;
for|for
control|(
name|FileStatus
name|storeFile
range|:
name|storeFiles
control|)
block|{
name|Path
name|storeFilePath
init|=
name|storeFile
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|FSDataInputStream
name|fsdis
init|=
literal|null
decl_stmt|;
name|long
name|lenToRead
init|=
literal|0
decl_stmt|;
try|try
block|{
comment|// check whether this path is a reference.
if|if
condition|(
name|StoreFileInfo
operator|.
name|isReference
argument_list|(
name|storeFilePath
argument_list|)
condition|)
continue|continue;
comment|// check whether this path is a HFileLink.
elseif|else
if|if
condition|(
name|HFileLink
operator|.
name|isHFileLink
argument_list|(
name|storeFilePath
argument_list|)
condition|)
block|{
name|FileLink
name|fLink
init|=
name|getFileLinkWithPreNSPath
argument_list|(
name|storeFilePath
argument_list|)
decl_stmt|;
name|fsdis
operator|=
name|fLink
operator|.
name|open
argument_list|(
name|fs
argument_list|)
expr_stmt|;
name|lenToRead
operator|=
name|fLink
operator|.
name|getFileStatus
argument_list|(
name|fs
argument_list|)
operator|.
name|getLen
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// a regular hfile
name|fsdis
operator|=
name|fs
operator|.
name|open
argument_list|(
name|storeFilePath
argument_list|)
expr_stmt|;
name|lenToRead
operator|=
name|storeFile
operator|.
name|getLen
argument_list|()
expr_stmt|;
block|}
name|int
name|majorVersion
init|=
name|computeMajorVersion
argument_list|(
name|fsdis
argument_list|,
name|lenToRead
argument_list|)
decl_stmt|;
if|if
condition|(
name|majorVersion
operator|==
literal|1
condition|)
block|{
name|hFileV1Set
operator|.
name|add
argument_list|(
name|storeFilePath
argument_list|)
expr_stmt|;
comment|// return this region path, as it needs to be compacted.
return|return
name|regionDir
return|;
block|}
if|if
condition|(
name|majorVersion
operator|>
literal|2
operator|||
name|majorVersion
operator|<
literal|1
condition|)
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Incorrect major version: "
operator|+
name|majorVersion
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|iae
parameter_list|)
block|{
name|corruptedHFiles
operator|.
name|add
argument_list|(
name|storeFilePath
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Got exception while reading trailer for file: "
operator|+
name|storeFilePath
argument_list|,
name|iae
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|fsdis
operator|!=
literal|null
condition|)
name|fsdis
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|int
name|computeMajorVersion
parameter_list|(
name|FSDataInputStream
name|istream
parameter_list|,
name|long
name|fileSize
parameter_list|)
throws|throws
name|IOException
block|{
comment|//read up the last int of the file. Major version is in the last 3 bytes.
name|long
name|seekPoint
init|=
name|fileSize
operator|-
name|Bytes
operator|.
name|SIZEOF_INT
decl_stmt|;
if|if
condition|(
name|seekPoint
operator|<
literal|0
condition|)
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"File too small, no major version found"
argument_list|)
throw|;
comment|// Read the version from the last int of the file.
name|istream
operator|.
name|seek
argument_list|(
name|seekPoint
argument_list|)
expr_stmt|;
name|int
name|version
init|=
name|istream
operator|.
name|readInt
argument_list|()
decl_stmt|;
comment|// Extract and return the major version
return|return
name|version
operator|&
literal|0x00ffffff
return|;
block|}
block|}
decl_stmt|;
name|Future
argument_list|<
name|Path
argument_list|>
name|f
init|=
name|exec
operator|.
name|submit
argument_list|(
name|regionCallable
argument_list|)
decl_stmt|;
return|return
name|f
return|;
block|}
comment|/**    * Creates a FileLink which adds pre-namespace paths in its list of available paths. This is used    * when reading a snapshot file in a pre-namespace file layout, for example, while upgrading.    * @param storeFilePath    * @return a FileLink which could read from pre-namespace paths.    * @throws IOException    */
specifier|public
name|FileLink
name|getFileLinkWithPreNSPath
parameter_list|(
name|Path
name|storeFilePath
parameter_list|)
throws|throws
name|IOException
block|{
name|HFileLink
name|link
init|=
name|HFileLink
operator|.
name|buildFromHFileLinkPattern
argument_list|(
name|getConf
argument_list|()
argument_list|,
name|storeFilePath
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|pathsToProcess
init|=
name|getPreNSPathsForHFileLink
argument_list|(
name|link
argument_list|)
decl_stmt|;
name|pathsToProcess
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|link
operator|.
name|getLocations
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|new
name|FileLink
argument_list|(
name|pathsToProcess
argument_list|)
return|;
block|}
specifier|private
name|List
argument_list|<
name|Path
argument_list|>
name|getPreNSPathsForHFileLink
parameter_list|(
name|HFileLink
name|fileLink
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|defaultNamespace
operator|==
literal|null
condition|)
name|setDefaultNamespaceDir
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|p
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
name|String
name|relativeTablePath
init|=
name|removeDefaultNSPath
argument_list|(
name|fileLink
operator|.
name|getOriginPath
argument_list|()
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|getPreNSPath
argument_list|(
name|PRE_NS_DOT_ARCHIVE
argument_list|,
name|relativeTablePath
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|getPreNSPath
argument_list|(
name|PRE_NS_DOT_TMP
argument_list|,
name|relativeTablePath
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|getPreNSPath
argument_list|(
literal|null
argument_list|,
name|relativeTablePath
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|p
return|;
block|}
comment|/**    * Removes the prefix of defaultNamespace from the path.    * @param originalPath    */
specifier|private
name|String
name|removeDefaultNSPath
parameter_list|(
name|Path
name|originalPath
parameter_list|)
block|{
name|String
name|pathStr
init|=
name|originalPath
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|pathStr
operator|.
name|startsWith
argument_list|(
name|defaultNamespace
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
return|return
name|pathStr
return|;
return|return
name|pathStr
operator|.
name|substring
argument_list|(
name|defaultNamespace
operator|.
name|toString
argument_list|()
operator|.
name|length
argument_list|()
operator|+
literal|1
argument_list|)
return|;
block|}
specifier|private
name|Path
name|getPreNSPath
parameter_list|(
name|String
name|prefix
parameter_list|,
name|String
name|relativeTablePath
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|relativePath
init|=
operator|(
name|prefix
operator|==
literal|null
condition|?
name|relativeTablePath
else|:
name|prefix
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
name|relativeTablePath
operator|)
decl_stmt|;
return|return
operator|new
name|Path
argument_list|(
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|getConf
argument_list|()
argument_list|)
argument_list|,
name|relativePath
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|boolean
name|isTableDir
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
comment|// check for old format, of having /table/.tableinfo; hbase:meta doesn't has .tableinfo,
comment|// include it.
if|if
condition|(
name|fs
operator|.
name|isFile
argument_list|(
name|path
argument_list|)
condition|)
return|return
literal|false
return|;
return|return
operator|(
name|FSTableDescriptors
operator|.
name|getTableInfoPath
argument_list|(
name|fs
argument_list|,
name|path
argument_list|)
operator|!=
literal|null
operator|||
name|FSTableDescriptors
operator|.
name|getCurrentTableInfoStatus
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
literal|false
argument_list|)
operator|!=
literal|null
operator|)
operator|||
name|path
operator|.
name|toString
argument_list|()
operator|.
name|endsWith
argument_list|(
literal|".META."
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|boolean
name|isRegionDir
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|fs
operator|.
name|isFile
argument_list|(
name|path
argument_list|)
condition|)
return|return
literal|false
return|;
name|Path
name|regionInfo
init|=
operator|new
name|Path
argument_list|(
name|path
argument_list|,
name|HRegionFileSystem
operator|.
name|REGION_INFO_FILE
argument_list|)
decl_stmt|;
return|return
name|fs
operator|.
name|exists
argument_list|(
name|regionInfo
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
name|args
index|[]
parameter_list|)
throws|throws
name|Exception
block|{
name|System
operator|.
name|exit
argument_list|(
name|ToolRunner
operator|.
name|run
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|,
operator|new
name|HFileV1Detector
argument_list|()
argument_list|,
name|args
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

