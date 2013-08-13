begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|hfile
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
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
name|ArrayList
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
name|Map
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
name|cli
operator|.
name|PosixParser
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
name|classification
operator|.
name|InterfaceAudience
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
name|classification
operator|.
name|InterfaceStability
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
name|TableName
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
name|HRegionInfo
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
name|KeyValue
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
name|hfile
operator|.
name|HFile
operator|.
name|FileInfo
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
name|TimeRangeTracker
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
name|BloomFilter
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
name|BloomFilterFactory
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
name|ByteBloomFilter
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
name|Bytes
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
name|Writables
import|;
end_import

begin_comment
comment|/**  * Implements pretty-printing functionality for {@link HFile}s.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|HFilePrettyPrinter
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
name|HFilePrettyPrinter
operator|.
name|class
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
specifier|private
name|boolean
name|verbose
decl_stmt|;
specifier|private
name|boolean
name|printValue
decl_stmt|;
specifier|private
name|boolean
name|printKey
decl_stmt|;
specifier|private
name|boolean
name|shouldPrintMeta
decl_stmt|;
specifier|private
name|boolean
name|printBlocks
decl_stmt|;
specifier|private
name|boolean
name|printStats
decl_stmt|;
specifier|private
name|boolean
name|checkRow
decl_stmt|;
specifier|private
name|boolean
name|checkFamily
decl_stmt|;
specifier|private
name|boolean
name|isSeekToRow
init|=
literal|false
decl_stmt|;
comment|/**    * The row which the user wants to specify and print all the KeyValues for.    */
specifier|private
name|byte
index|[]
name|row
init|=
literal|null
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Path
argument_list|>
name|files
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|int
name|count
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FOUR_SPACES
init|=
literal|"    "
decl_stmt|;
specifier|public
name|HFilePrettyPrinter
parameter_list|()
block|{
name|options
operator|.
name|addOption
argument_list|(
literal|"v"
argument_list|,
literal|"verbose"
argument_list|,
literal|false
argument_list|,
literal|"Verbose output; emits file and meta data delimiters"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|"p"
argument_list|,
literal|"printkv"
argument_list|,
literal|false
argument_list|,
literal|"Print key/value pairs"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|"e"
argument_list|,
literal|"printkey"
argument_list|,
literal|false
argument_list|,
literal|"Print keys"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|"m"
argument_list|,
literal|"printmeta"
argument_list|,
literal|false
argument_list|,
literal|"Print meta data of file"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|"b"
argument_list|,
literal|"printblocks"
argument_list|,
literal|false
argument_list|,
literal|"Print block index meta data"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|"k"
argument_list|,
literal|"checkrow"
argument_list|,
literal|false
argument_list|,
literal|"Enable row order check; looks for out-of-order keys"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|"a"
argument_list|,
literal|"checkfamily"
argument_list|,
literal|false
argument_list|,
literal|"Enable family check"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|"f"
argument_list|,
literal|"file"
argument_list|,
literal|true
argument_list|,
literal|"File to scan. Pass full-path; e.g. hdfs://a:9000/hbase/.META./12/34"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|"w"
argument_list|,
literal|"seekToRow"
argument_list|,
literal|true
argument_list|,
literal|"Seek to this row and print all the kvs for this row only"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|"r"
argument_list|,
literal|"region"
argument_list|,
literal|true
argument_list|,
literal|"Region to scan. Pass region name; e.g. '.META.,,1'"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|"s"
argument_list|,
literal|"stats"
argument_list|,
literal|false
argument_list|,
literal|"Print statistics"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|parseOptions
parameter_list|(
name|String
name|args
index|[]
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
literal|"HFile"
argument_list|,
name|options
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|CommandLineParser
name|parser
init|=
operator|new
name|PosixParser
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
name|verbose
operator|=
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"v"
argument_list|)
expr_stmt|;
name|printValue
operator|=
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"p"
argument_list|)
expr_stmt|;
name|printKey
operator|=
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"e"
argument_list|)
operator|||
name|printValue
expr_stmt|;
name|shouldPrintMeta
operator|=
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"m"
argument_list|)
expr_stmt|;
name|printBlocks
operator|=
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"b"
argument_list|)
expr_stmt|;
name|printStats
operator|=
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"s"
argument_list|)
expr_stmt|;
name|checkRow
operator|=
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"k"
argument_list|)
expr_stmt|;
name|checkFamily
operator|=
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"a"
argument_list|)
expr_stmt|;
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"f"
argument_list|)
condition|)
block|{
name|files
operator|.
name|add
argument_list|(
operator|new
name|Path
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
literal|"f"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"w"
argument_list|)
condition|)
block|{
name|String
name|key
init|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
literal|"w"
argument_list|)
decl_stmt|;
if|if
condition|(
name|key
operator|!=
literal|null
operator|&&
name|key
operator|.
name|length
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|row
operator|=
name|key
operator|.
name|getBytes
argument_list|()
expr_stmt|;
name|isSeekToRow
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Invalid row is specified."
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"r"
argument_list|)
condition|)
block|{
name|String
name|regionName
init|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
literal|"r"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|rn
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|regionName
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|hri
init|=
name|HRegionInfo
operator|.
name|parseRegionName
argument_list|(
name|rn
argument_list|)
decl_stmt|;
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
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|rootDir
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|hri
index|[
literal|0
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|enc
init|=
name|HRegionInfo
operator|.
name|encodeRegionName
argument_list|(
name|rn
argument_list|)
decl_stmt|;
name|Path
name|regionDir
init|=
operator|new
name|Path
argument_list|(
name|tableDir
argument_list|,
name|enc
argument_list|)
decl_stmt|;
if|if
condition|(
name|verbose
condition|)
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"region dir -> "
operator|+
name|regionDir
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|regionFiles
init|=
name|HFile
operator|.
name|getStoreFiles
argument_list|(
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
argument_list|,
name|regionDir
argument_list|)
decl_stmt|;
if|if
condition|(
name|verbose
condition|)
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Number of region files found -> "
operator|+
name|regionFiles
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|verbose
condition|)
block|{
name|int
name|i
init|=
literal|1
decl_stmt|;
for|for
control|(
name|Path
name|p
range|:
name|regionFiles
control|)
block|{
if|if
condition|(
name|verbose
condition|)
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Found file["
operator|+
name|i
operator|++
operator|+
literal|"] -> "
operator|+
name|p
argument_list|)
expr_stmt|;
block|}
block|}
name|files
operator|.
name|addAll
argument_list|(
name|regionFiles
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
comment|/**    * Runs the command-line pretty-printer, and returns the desired command    * exit code (zero for success, non-zero for failure).    */
specifier|public
name|int
name|run
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
expr_stmt|;
try|try
block|{
name|FSUtils
operator|.
name|setFsDefault
argument_list|(
name|conf
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|parseOptions
argument_list|(
name|args
argument_list|)
condition|)
return|return
literal|1
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
name|error
argument_list|(
literal|"Error parsing command-line options"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error parsing command-line options"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
comment|// iterate over all files found
for|for
control|(
name|Path
name|fileName
range|:
name|files
control|)
block|{
try|try
block|{
name|processFile
argument_list|(
name|fileName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error reading "
operator|+
name|fileName
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|verbose
operator|||
name|printKey
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Scanned kv count -> "
operator|+
name|count
argument_list|)
expr_stmt|;
block|}
return|return
literal|0
return|;
block|}
specifier|private
name|void
name|processFile
parameter_list|(
name|Path
name|file
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|verbose
condition|)
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Scanning -> "
operator|+
name|file
argument_list|)
expr_stmt|;
name|FileSystem
name|fs
init|=
name|file
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|file
argument_list|)
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"ERROR, file doesnt exist: "
operator|+
name|file
argument_list|)
expr_stmt|;
block|}
name|HFile
operator|.
name|Reader
name|reader
init|=
name|HFile
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|file
argument_list|,
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|fileInfo
init|=
name|reader
operator|.
name|loadFileInfo
argument_list|()
decl_stmt|;
name|KeyValueStatsCollector
name|fileStats
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|verbose
operator|||
name|printKey
operator|||
name|checkRow
operator|||
name|checkFamily
operator|||
name|printStats
condition|)
block|{
comment|// scan over file and read key/value's and check if requested
name|HFileScanner
name|scanner
init|=
name|reader
operator|.
name|getScanner
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|fileStats
operator|=
operator|new
name|KeyValueStatsCollector
argument_list|()
expr_stmt|;
name|boolean
name|shouldScanKeysValues
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|isSeekToRow
condition|)
block|{
comment|// seek to the first kv on this row
name|shouldScanKeysValues
operator|=
operator|(
name|scanner
operator|.
name|seekTo
argument_list|(
name|KeyValue
operator|.
name|createFirstOnRow
argument_list|(
name|this
operator|.
name|row
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
operator|!=
operator|-
literal|1
operator|)
expr_stmt|;
block|}
else|else
block|{
name|shouldScanKeysValues
operator|=
name|scanner
operator|.
name|seekTo
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|shouldScanKeysValues
condition|)
name|scanKeysValues
argument_list|(
name|file
argument_list|,
name|fileStats
argument_list|,
name|scanner
argument_list|,
name|row
argument_list|)
expr_stmt|;
block|}
comment|// print meta data
if|if
condition|(
name|shouldPrintMeta
condition|)
block|{
name|printMeta
argument_list|(
name|reader
argument_list|,
name|fileInfo
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|printBlocks
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Block Index:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|reader
operator|.
name|getDataBlockIndexReader
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|printStats
condition|)
block|{
name|fileStats
operator|.
name|finish
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Stats:\n"
operator|+
name|fileStats
argument_list|)
expr_stmt|;
block|}
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|scanKeysValues
parameter_list|(
name|Path
name|file
parameter_list|,
name|KeyValueStatsCollector
name|fileStats
parameter_list|,
name|HFileScanner
name|scanner
parameter_list|,
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
block|{
name|KeyValue
name|pkv
init|=
literal|null
decl_stmt|;
do|do
block|{
name|KeyValue
name|kv
init|=
name|scanner
operator|.
name|getKeyValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|row
operator|!=
literal|null
operator|&&
name|row
operator|.
name|length
operator|!=
literal|0
condition|)
block|{
name|int
name|result
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|kv
operator|.
name|getRow
argument_list|()
argument_list|,
name|row
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|>
literal|0
condition|)
block|{
break|break;
block|}
elseif|else
if|if
condition|(
name|result
operator|<
literal|0
condition|)
block|{
continue|continue;
block|}
block|}
comment|// collect stats
if|if
condition|(
name|printStats
condition|)
block|{
name|fileStats
operator|.
name|collect
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
comment|// dump key value
if|if
condition|(
name|printKey
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|print
argument_list|(
literal|"K: "
operator|+
name|kv
argument_list|)
expr_stmt|;
if|if
condition|(
name|printValue
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|print
argument_list|(
literal|" V: "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|kv
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|()
expr_stmt|;
block|}
comment|// check if rows are in order
if|if
condition|(
name|checkRow
operator|&&
name|pkv
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|pkv
operator|.
name|getRow
argument_list|()
argument_list|,
name|kv
operator|.
name|getRow
argument_list|()
argument_list|)
operator|>
literal|0
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"WARNING, previous row is greater then"
operator|+
literal|" current row\n\tfilename -> "
operator|+
name|file
operator|+
literal|"\n\tprevious -> "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|pkv
operator|.
name|getKey
argument_list|()
argument_list|)
operator|+
literal|"\n\tcurrent  -> "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|kv
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// check if families are consistent
if|if
condition|(
name|checkFamily
condition|)
block|{
name|String
name|fam
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|file
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
name|fam
argument_list|)
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"WARNING, filename does not match kv family,"
operator|+
literal|"\n\tfilename -> "
operator|+
name|file
operator|+
literal|"\n\tkeyvalue -> "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|kv
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|pkv
operator|!=
literal|null
operator|&&
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|pkv
operator|.
name|getFamily
argument_list|()
argument_list|,
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|)
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"WARNING, previous kv has different family"
operator|+
literal|" compared to current key\n\tfilename -> "
operator|+
name|file
operator|+
literal|"\n\tprevious -> "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|pkv
operator|.
name|getKey
argument_list|()
argument_list|)
operator|+
literal|"\n\tcurrent  -> "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|kv
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|pkv
operator|=
name|kv
expr_stmt|;
operator|++
name|count
expr_stmt|;
block|}
do|while
condition|(
name|scanner
operator|.
name|next
argument_list|()
condition|)
do|;
block|}
comment|/**    * Format a string of the form "k1=v1, k2=v2, ..." into separate lines    * with a four-space indentation.    */
specifier|private
specifier|static
name|String
name|asSeparateLines
parameter_list|(
name|String
name|keyValueStr
parameter_list|)
block|{
return|return
name|keyValueStr
operator|.
name|replaceAll
argument_list|(
literal|", ([a-zA-Z]+=)"
argument_list|,
literal|",\n"
operator|+
name|FOUR_SPACES
operator|+
literal|"$1"
argument_list|)
return|;
block|}
specifier|private
name|void
name|printMeta
parameter_list|(
name|HFile
operator|.
name|Reader
name|reader
parameter_list|,
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|fileInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Block index size as per heapsize: "
operator|+
name|reader
operator|.
name|indexSize
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|asSeparateLines
argument_list|(
name|reader
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Trailer:\n    "
operator|+
name|asSeparateLines
argument_list|(
name|reader
operator|.
name|getTrailer
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Fileinfo:"
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|e
range|:
name|fileInfo
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|System
operator|.
name|out
operator|.
name|print
argument_list|(
name|FOUR_SPACES
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
operator|+
literal|" = "
argument_list|)
expr_stmt|;
if|if
condition|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"MAX_SEQ_ID_KEY"
argument_list|)
argument_list|)
operator|==
literal|0
condition|)
block|{
name|long
name|seqid
init|=
name|Bytes
operator|.
name|toLong
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|seqid
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"TIMERANGE"
argument_list|)
argument_list|)
operator|==
literal|0
condition|)
block|{
name|TimeRangeTracker
name|timeRangeTracker
init|=
operator|new
name|TimeRangeTracker
argument_list|()
decl_stmt|;
name|Writables
operator|.
name|copyWritable
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|,
name|timeRangeTracker
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|timeRangeTracker
operator|.
name|getMinimumTimestamp
argument_list|()
operator|+
literal|"...."
operator|+
name|timeRangeTracker
operator|.
name|getMaximumTimestamp
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|FileInfo
operator|.
name|AVG_KEY_LEN
argument_list|)
operator|==
literal|0
operator|||
name|Bytes
operator|.
name|compareTo
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|FileInfo
operator|.
name|AVG_VALUE_LEN
argument_list|)
operator|==
literal|0
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|Bytes
operator|.
name|toInt
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Mid-key: "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|reader
operator|.
name|midkey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Printing general bloom information
name|DataInput
name|bloomMeta
init|=
name|reader
operator|.
name|getGeneralBloomFilterMetadata
argument_list|()
decl_stmt|;
name|BloomFilter
name|bloomFilter
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|bloomMeta
operator|!=
literal|null
condition|)
name|bloomFilter
operator|=
name|BloomFilterFactory
operator|.
name|createFromMeta
argument_list|(
name|bloomMeta
argument_list|,
name|reader
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Bloom filter:"
argument_list|)
expr_stmt|;
if|if
condition|(
name|bloomFilter
operator|!=
literal|null
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|FOUR_SPACES
operator|+
name|bloomFilter
operator|.
name|toString
argument_list|()
operator|.
name|replaceAll
argument_list|(
name|ByteBloomFilter
operator|.
name|STATS_RECORD_SEP
argument_list|,
literal|"\n"
operator|+
name|FOUR_SPACES
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|FOUR_SPACES
operator|+
literal|"Not present"
argument_list|)
expr_stmt|;
block|}
comment|// Printing delete bloom information
name|bloomMeta
operator|=
name|reader
operator|.
name|getDeleteBloomFilterMetadata
argument_list|()
expr_stmt|;
name|bloomFilter
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|bloomMeta
operator|!=
literal|null
condition|)
name|bloomFilter
operator|=
name|BloomFilterFactory
operator|.
name|createFromMeta
argument_list|(
name|bloomMeta
argument_list|,
name|reader
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Delete Family Bloom filter:"
argument_list|)
expr_stmt|;
if|if
condition|(
name|bloomFilter
operator|!=
literal|null
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|FOUR_SPACES
operator|+
name|bloomFilter
operator|.
name|toString
argument_list|()
operator|.
name|replaceAll
argument_list|(
name|ByteBloomFilter
operator|.
name|STATS_RECORD_SEP
argument_list|,
literal|"\n"
operator|+
name|FOUR_SPACES
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|FOUR_SPACES
operator|+
literal|"Not present"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|LongStats
block|{
specifier|private
name|long
name|min
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
specifier|private
name|long
name|max
init|=
name|Long
operator|.
name|MIN_VALUE
decl_stmt|;
specifier|private
name|long
name|sum
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|count
init|=
literal|0
decl_stmt|;
name|void
name|collect
parameter_list|(
name|long
name|d
parameter_list|)
block|{
if|if
condition|(
name|d
operator|<
name|min
condition|)
name|min
operator|=
name|d
expr_stmt|;
if|if
condition|(
name|d
operator|>
name|max
condition|)
name|max
operator|=
name|d
expr_stmt|;
name|sum
operator|+=
name|d
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"count: "
operator|+
name|count
operator|+
literal|"\tmin: "
operator|+
name|min
operator|+
literal|"\tmax: "
operator|+
name|max
operator|+
literal|"\tmean: "
operator|+
operator|(
operator|(
name|double
operator|)
name|sum
operator|/
name|count
operator|)
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|KeyValueStatsCollector
block|{
name|LongStats
name|keyLen
init|=
operator|new
name|LongStats
argument_list|()
decl_stmt|;
name|LongStats
name|valLen
init|=
operator|new
name|LongStats
argument_list|()
decl_stmt|;
name|LongStats
name|rowSizeBytes
init|=
operator|new
name|LongStats
argument_list|()
decl_stmt|;
name|LongStats
name|rowSizeCols
init|=
operator|new
name|LongStats
argument_list|()
decl_stmt|;
name|long
name|curRowBytes
init|=
literal|0
decl_stmt|;
name|long
name|curRowCols
init|=
literal|0
decl_stmt|;
name|byte
index|[]
name|biggestRow
init|=
literal|null
decl_stmt|;
specifier|private
name|KeyValue
name|prevKV
init|=
literal|null
decl_stmt|;
specifier|private
name|long
name|maxRowBytes
init|=
literal|0
decl_stmt|;
specifier|public
name|void
name|collect
parameter_list|(
name|KeyValue
name|kv
parameter_list|)
block|{
name|keyLen
operator|.
name|collect
argument_list|(
name|kv
operator|.
name|getKeyLength
argument_list|()
argument_list|)
expr_stmt|;
name|valLen
operator|.
name|collect
argument_list|(
name|kv
operator|.
name|getValueLength
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|prevKV
operator|!=
literal|null
operator|&&
name|KeyValue
operator|.
name|COMPARATOR
operator|.
name|compareRows
argument_list|(
name|prevKV
argument_list|,
name|kv
argument_list|)
operator|!=
literal|0
condition|)
block|{
comment|// new row
name|collectRow
argument_list|()
expr_stmt|;
block|}
name|curRowBytes
operator|+=
name|kv
operator|.
name|getLength
argument_list|()
expr_stmt|;
name|curRowCols
operator|++
expr_stmt|;
name|prevKV
operator|=
name|kv
expr_stmt|;
block|}
specifier|private
name|void
name|collectRow
parameter_list|()
block|{
name|rowSizeBytes
operator|.
name|collect
argument_list|(
name|curRowBytes
argument_list|)
expr_stmt|;
name|rowSizeCols
operator|.
name|collect
argument_list|(
name|curRowCols
argument_list|)
expr_stmt|;
if|if
condition|(
name|curRowBytes
operator|>
name|maxRowBytes
operator|&&
name|prevKV
operator|!=
literal|null
condition|)
block|{
name|biggestRow
operator|=
name|prevKV
operator|.
name|getRow
argument_list|()
expr_stmt|;
name|maxRowBytes
operator|=
name|curRowBytes
expr_stmt|;
block|}
name|curRowBytes
operator|=
literal|0
expr_stmt|;
name|curRowCols
operator|=
literal|0
expr_stmt|;
block|}
specifier|public
name|void
name|finish
parameter_list|()
block|{
if|if
condition|(
name|curRowCols
operator|>
literal|0
condition|)
block|{
name|collectRow
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
if|if
condition|(
name|prevKV
operator|==
literal|null
condition|)
return|return
literal|"no data available for statistics"
return|;
return|return
literal|"Key length: "
operator|+
name|keyLen
operator|+
literal|"\n"
operator|+
literal|"Val length: "
operator|+
name|valLen
operator|+
literal|"\n"
operator|+
literal|"Row size (bytes): "
operator|+
name|rowSizeBytes
operator|+
literal|"\n"
operator|+
literal|"Row size (columns): "
operator|+
name|rowSizeCols
operator|+
literal|"\n"
operator|+
literal|"Key of biggest row: "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|biggestRow
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

