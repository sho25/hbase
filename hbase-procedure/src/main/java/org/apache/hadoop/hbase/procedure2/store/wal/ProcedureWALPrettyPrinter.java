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
name|procedure2
operator|.
name|store
operator|.
name|wal
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
name|io
operator|.
name|PrintStream
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
name|procedure2
operator|.
name|Procedure
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
name|procedure2
operator|.
name|ProcedureUtil
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
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
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
name|DefaultParser
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
name|HelpFormatter
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
name|Options
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
name|ParseException
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ProcedureProtos
operator|.
name|ProcedureWALEntry
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ProcedureProtos
operator|.
name|ProcedureWALHeader
import|;
end_import

begin_comment
comment|/**  * ProcedureWALPrettyPrinter prints the contents of a given ProcedureWAL file  * @see WALProcedureStore#main(String[]) if you want to check parse of a directory of WALs.  * @deprecated Since 2.3.0, will be removed in 4.0.0. Keep here only for rolling upgrading, now we  *             use the new region based procedure store.  */
end_comment

begin_class
annotation|@
name|Deprecated
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|TOOLS
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|ProcedureWALPrettyPrinter
extends|extends
name|Configured
implements|implements
name|Tool
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
name|ProcedureWALPrettyPrinter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|PrintStream
name|out
decl_stmt|;
specifier|public
name|ProcedureWALPrettyPrinter
parameter_list|()
block|{
name|out
operator|=
name|System
operator|.
name|out
expr_stmt|;
block|}
comment|/**    * Reads a log file and outputs its contents.    *    * @param conf   HBase configuration relevant to this log file    * @param p       path of the log file to be read    * @throws IOException  IOException    */
specifier|public
name|void
name|processFile
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Path
name|p
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|p
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
name|p
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
name|p
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
operator|!
name|fs
operator|.
name|isFile
argument_list|(
name|p
argument_list|)
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|p
operator|+
literal|" is not a file"
argument_list|)
expr_stmt|;
return|return;
block|}
name|FileStatus
name|logFile
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|p
argument_list|)
decl_stmt|;
if|if
condition|(
name|logFile
operator|.
name|getLen
argument_list|()
operator|==
literal|0
condition|)
block|{
name|out
operator|.
name|println
argument_list|(
literal|"Zero length file: "
operator|+
name|p
argument_list|)
expr_stmt|;
return|return;
block|}
name|out
operator|.
name|println
argument_list|(
literal|"Opening procedure state-log: "
operator|+
name|p
argument_list|)
expr_stmt|;
name|ProcedureWALFile
name|log
init|=
operator|new
name|ProcedureWALFile
argument_list|(
name|fs
argument_list|,
name|logFile
argument_list|)
decl_stmt|;
name|processProcedureWALFile
argument_list|(
name|log
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|processProcedureWALFile
parameter_list|(
name|ProcedureWALFile
name|log
parameter_list|)
throws|throws
name|IOException
block|{
name|log
operator|.
name|open
argument_list|()
expr_stmt|;
name|ProcedureWALHeader
name|header
init|=
name|log
operator|.
name|getHeader
argument_list|()
decl_stmt|;
name|printHeader
argument_list|(
name|header
argument_list|)
expr_stmt|;
name|FSDataInputStream
name|stream
init|=
name|log
operator|.
name|getStream
argument_list|()
decl_stmt|;
try|try
block|{
name|boolean
name|hasMore
init|=
literal|true
decl_stmt|;
while|while
condition|(
name|hasMore
condition|)
block|{
name|ProcedureWALEntry
name|entry
init|=
name|ProcedureWALFormat
operator|.
name|readEntry
argument_list|(
name|stream
argument_list|)
decl_stmt|;
if|if
condition|(
name|entry
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|println
argument_list|(
literal|"No more entry, exiting with missing EOF"
argument_list|)
expr_stmt|;
name|hasMore
operator|=
literal|false
expr_stmt|;
break|break;
block|}
switch|switch
condition|(
name|entry
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|PROCEDURE_WAL_EOF
case|:
name|hasMore
operator|=
literal|false
expr_stmt|;
break|break;
default|default:
name|printEntry
argument_list|(
name|entry
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|out
operator|.
name|println
argument_list|(
literal|"got an exception while reading the procedure WAL "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|log
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|printEntry
parameter_list|(
specifier|final
name|ProcedureWALEntry
name|entry
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|println
argument_list|(
literal|"EntryType="
operator|+
name|entry
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|procCount
init|=
name|entry
operator|.
name|getProcedureCount
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|procCount
condition|;
name|i
operator|++
control|)
block|{
name|Procedure
argument_list|<
name|?
argument_list|>
name|proc
init|=
name|ProcedureUtil
operator|.
name|convertToProcedure
argument_list|(
name|entry
operator|.
name|getProcedure
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|printProcedure
argument_list|(
name|proc
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|printProcedure
parameter_list|(
name|Procedure
argument_list|<
name|?
argument_list|>
name|proc
parameter_list|)
block|{
name|out
operator|.
name|println
argument_list|(
name|proc
operator|.
name|toStringDetails
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|printHeader
parameter_list|(
name|ProcedureWALHeader
name|header
parameter_list|)
block|{
name|out
operator|.
name|println
argument_list|(
literal|"ProcedureWALHeader: "
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"  Version: "
operator|+
name|header
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"  Type: "
operator|+
name|header
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"  LogId: "
operator|+
name|header
operator|.
name|getLogId
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"  MinProcId: "
operator|+
name|header
operator|.
name|getMinProcId
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|()
expr_stmt|;
block|}
comment|/**    * Pass one or more log file names and formatting options and it will dump out    * a text version of the contents on<code>stdout</code>.    *    * @param args    *          Command line arguments    * @throws IOException    *           Thrown upon file system errors etc.    */
annotation|@
name|Override
specifier|public
name|int
name|run
parameter_list|(
specifier|final
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
comment|// create options
name|Options
name|options
init|=
operator|new
name|Options
argument_list|()
decl_stmt|;
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
literal|"Output help message"
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
literal|"File to print"
argument_list|)
expr_stmt|;
specifier|final
name|List
argument_list|<
name|Path
argument_list|>
name|files
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
try|try
block|{
name|CommandLine
name|cmd
init|=
operator|new
name|DefaultParser
argument_list|()
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
name|files
operator|.
name|isEmpty
argument_list|()
operator|||
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
literal|"ProcedureWALPrettyPrinter "
argument_list|,
name|options
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
operator|(
operator|-
literal|1
operator|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|ParseException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to parse commandLine arguments"
argument_list|,
name|e
argument_list|)
expr_stmt|;
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
literal|"ProcedureWALPrettyPrinter "
argument_list|,
name|options
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
operator|(
operator|-
literal|1
operator|)
return|;
block|}
comment|// get configuration, file system, and process the given files
for|for
control|(
name|Path
name|file
range|:
name|files
control|)
block|{
name|processFile
argument_list|(
name|getConf
argument_list|()
argument_list|,
name|file
argument_list|)
expr_stmt|;
block|}
return|return
operator|(
literal|0
operator|)
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
throws|throws
name|Exception
block|{
specifier|final
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|int
name|exitCode
init|=
name|ToolRunner
operator|.
name|run
argument_list|(
name|conf
argument_list|,
operator|new
name|ProcedureWALPrettyPrinter
argument_list|()
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|exitCode
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

