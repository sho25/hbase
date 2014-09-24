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
name|mapreduce
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
name|HashMap
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
name|hadoop
operator|.
name|hbase
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
name|hbase
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
name|client
operator|.
name|Scan
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
name|mapreduce
operator|.
name|Job
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
name|GenericOptionsParser
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
comment|/**  * Tool used to copy a table to another one which can be on a different setup.  * It is also configurable with a start and time as well as a specification  * of the region server implementation if different from the local cluster.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|CopyTable
extends|extends
name|Configured
implements|implements
name|Tool
block|{
specifier|final
specifier|static
name|String
name|NAME
init|=
literal|"copytable"
decl_stmt|;
specifier|static
name|long
name|startTime
init|=
literal|0
decl_stmt|;
specifier|static
name|long
name|endTime
init|=
literal|0
decl_stmt|;
specifier|static
name|int
name|versions
init|=
operator|-
literal|1
decl_stmt|;
specifier|static
name|String
name|tableName
init|=
literal|null
decl_stmt|;
specifier|static
name|String
name|startRow
init|=
literal|null
decl_stmt|;
specifier|static
name|String
name|stopRow
init|=
literal|null
decl_stmt|;
specifier|static
name|String
name|newTableName
init|=
literal|null
decl_stmt|;
specifier|static
name|String
name|peerAddress
init|=
literal|null
decl_stmt|;
specifier|static
name|String
name|families
init|=
literal|null
decl_stmt|;
specifier|static
name|boolean
name|allCells
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|JOB_NAME_CONF_KEY
init|=
literal|"mapreduce.job.name"
decl_stmt|;
specifier|public
name|CopyTable
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * Sets up the actual job.    *    * @param conf  The current configuration.    * @param args  The command line parameters.    * @return The newly created job.    * @throws IOException When setting up the job fails.    */
specifier|public
specifier|static
name|Job
name|createSubmittableJob
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|doCommandLine
argument_list|(
name|args
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Job
name|job
init|=
name|Job
operator|.
name|getInstance
argument_list|(
name|conf
argument_list|,
name|conf
operator|.
name|get
argument_list|(
name|JOB_NAME_CONF_KEY
argument_list|,
name|NAME
operator|+
literal|"_"
operator|+
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|job
operator|.
name|setJarByClass
argument_list|(
name|CopyTable
operator|.
name|class
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setCacheBlocks
argument_list|(
literal|false
argument_list|)
expr_stmt|;
if|if
condition|(
name|startTime
operator|!=
literal|0
condition|)
block|{
name|scan
operator|.
name|setTimeRange
argument_list|(
name|startTime
argument_list|,
name|endTime
operator|==
literal|0
condition|?
name|HConstants
operator|.
name|LATEST_TIMESTAMP
else|:
name|endTime
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|allCells
condition|)
block|{
name|scan
operator|.
name|setRaw
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|versions
operator|>=
literal|0
condition|)
block|{
name|scan
operator|.
name|setMaxVersions
argument_list|(
name|versions
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|startRow
operator|!=
literal|null
condition|)
block|{
name|scan
operator|.
name|setStartRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|startRow
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|stopRow
operator|!=
literal|null
condition|)
block|{
name|scan
operator|.
name|setStopRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|stopRow
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|families
operator|!=
literal|null
condition|)
block|{
name|String
index|[]
name|fams
init|=
name|families
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|cfRenameMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|fam
range|:
name|fams
control|)
block|{
name|String
name|sourceCf
decl_stmt|;
if|if
condition|(
name|fam
operator|.
name|contains
argument_list|(
literal|":"
argument_list|)
condition|)
block|{
comment|// fam looks like "sourceCfName:destCfName"
name|String
index|[]
name|srcAndDest
init|=
name|fam
operator|.
name|split
argument_list|(
literal|":"
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|sourceCf
operator|=
name|srcAndDest
index|[
literal|0
index|]
expr_stmt|;
name|String
name|destCf
init|=
name|srcAndDest
index|[
literal|1
index|]
decl_stmt|;
name|cfRenameMap
operator|.
name|put
argument_list|(
name|sourceCf
argument_list|,
name|destCf
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// fam is just "sourceCf"
name|sourceCf
operator|=
name|fam
expr_stmt|;
block|}
name|scan
operator|.
name|addFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|sourceCf
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Import
operator|.
name|configureCfRenaming
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|cfRenameMap
argument_list|)
expr_stmt|;
block|}
name|TableMapReduceUtil
operator|.
name|initTableMapperJob
argument_list|(
name|tableName
argument_list|,
name|scan
argument_list|,
name|Import
operator|.
name|Importer
operator|.
name|class
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|job
argument_list|)
expr_stmt|;
name|TableMapReduceUtil
operator|.
name|initTableReducerJob
argument_list|(
name|newTableName
operator|==
literal|null
condition|?
name|tableName
else|:
name|newTableName
argument_list|,
literal|null
argument_list|,
name|job
argument_list|,
literal|null
argument_list|,
name|peerAddress
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|job
operator|.
name|setNumReduceTasks
argument_list|(
literal|0
argument_list|)
expr_stmt|;
return|return
name|job
return|;
block|}
comment|/*    * @param errorMsg Error message.  Can be null.    */
specifier|private
specifier|static
name|void
name|printUsage
parameter_list|(
specifier|final
name|String
name|errorMsg
parameter_list|)
block|{
if|if
condition|(
name|errorMsg
operator|!=
literal|null
operator|&&
name|errorMsg
operator|.
name|length
argument_list|()
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
literal|"ERROR: "
operator|+
name|errorMsg
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Usage: CopyTable [general options] [--starttime=X] [--endtime=Y] "
operator|+
literal|"[--new.name=NEW] [--peer.adr=ADR]<tablename>"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|()
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Options:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" rs.class     hbase.regionserver.class of the peer cluster"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"              specify if different from current cluster"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" rs.impl      hbase.regionserver.impl of the peer cluster"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" startrow     the start row"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" stoprow      the stop row"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" starttime    beginning of the time range (unixtime in millis)"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"              without endtime means from starttime to forever"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" endtime      end of the time range.  Ignored if no starttime specified."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" versions     number of cell versions to copy"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" new.name     new table's name"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" peer.adr     Address of the peer cluster given in the format"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"              hbase.zookeeer.quorum:hbase.zookeeper.client.port:zookeeper.znode.parent"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" families     comma-separated list of families to copy"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"              To copy from cf1 to cf2, give sourceCfName:destCfName. "
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"              To keep the same name, just give \"cfName\""
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" all.cells    also copy delete markers and deleted cells"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|()
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Args:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" tablename    Name of the table to copy"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|()
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Examples:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" To copy 'TestTable' to a cluster that uses replication for a 1 hour window:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" $ bin/hbase "
operator|+
literal|"org.apache.hadoop.hbase.mapreduce.CopyTable --starttime=1265875194289 --endtime=1265878794289 "
operator|+
literal|"--peer.adr=server1,server2,server3:2181:/hbase --families=myOldCf:myNewCf,cf2,cf3 TestTable "
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"For performance consider the following general option:\n"
operator|+
literal|"  It is recommended that you set the following to>=100. A higher value uses more memory but\n"
operator|+
literal|"  decreases the round trip time to the server and may increase performance.\n"
operator|+
literal|"    -Dhbase.client.scanner.caching=100\n"
operator|+
literal|"  The following should always be set to false, to prevent writing data twice, which may produce \n"
operator|+
literal|"  inaccurate results.\n"
operator|+
literal|"    -Dmapreduce.map.speculative=false"
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|boolean
name|doCommandLine
parameter_list|(
specifier|final
name|String
index|[]
name|args
parameter_list|)
block|{
comment|// Process command-line args. TODO: Better cmd-line processing
comment|// (but hopefully something not as painful as cli options).
if|if
condition|(
name|args
operator|.
name|length
operator|<
literal|1
condition|)
block|{
name|printUsage
argument_list|(
literal|null
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
try|try
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|args
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|cmd
init|=
name|args
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-h"
argument_list|)
operator|||
name|cmd
operator|.
name|startsWith
argument_list|(
literal|"--h"
argument_list|)
condition|)
block|{
name|printUsage
argument_list|(
literal|null
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
specifier|final
name|String
name|startRowArgKey
init|=
literal|"--startrow="
decl_stmt|;
if|if
condition|(
name|cmd
operator|.
name|startsWith
argument_list|(
name|startRowArgKey
argument_list|)
condition|)
block|{
name|startRow
operator|=
name|cmd
operator|.
name|substring
argument_list|(
name|startRowArgKey
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
specifier|final
name|String
name|stopRowArgKey
init|=
literal|"--stoprow="
decl_stmt|;
if|if
condition|(
name|cmd
operator|.
name|startsWith
argument_list|(
name|stopRowArgKey
argument_list|)
condition|)
block|{
name|stopRow
operator|=
name|cmd
operator|.
name|substring
argument_list|(
name|stopRowArgKey
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
specifier|final
name|String
name|startTimeArgKey
init|=
literal|"--starttime="
decl_stmt|;
if|if
condition|(
name|cmd
operator|.
name|startsWith
argument_list|(
name|startTimeArgKey
argument_list|)
condition|)
block|{
name|startTime
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|cmd
operator|.
name|substring
argument_list|(
name|startTimeArgKey
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
continue|continue;
block|}
specifier|final
name|String
name|endTimeArgKey
init|=
literal|"--endtime="
decl_stmt|;
if|if
condition|(
name|cmd
operator|.
name|startsWith
argument_list|(
name|endTimeArgKey
argument_list|)
condition|)
block|{
name|endTime
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|cmd
operator|.
name|substring
argument_list|(
name|endTimeArgKey
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
continue|continue;
block|}
specifier|final
name|String
name|versionsArgKey
init|=
literal|"--versions="
decl_stmt|;
if|if
condition|(
name|cmd
operator|.
name|startsWith
argument_list|(
name|versionsArgKey
argument_list|)
condition|)
block|{
name|versions
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmd
operator|.
name|substring
argument_list|(
name|versionsArgKey
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
continue|continue;
block|}
specifier|final
name|String
name|newNameArgKey
init|=
literal|"--new.name="
decl_stmt|;
if|if
condition|(
name|cmd
operator|.
name|startsWith
argument_list|(
name|newNameArgKey
argument_list|)
condition|)
block|{
name|newTableName
operator|=
name|cmd
operator|.
name|substring
argument_list|(
name|newNameArgKey
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
specifier|final
name|String
name|peerAdrArgKey
init|=
literal|"--peer.adr="
decl_stmt|;
if|if
condition|(
name|cmd
operator|.
name|startsWith
argument_list|(
name|peerAdrArgKey
argument_list|)
condition|)
block|{
name|peerAddress
operator|=
name|cmd
operator|.
name|substring
argument_list|(
name|peerAdrArgKey
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
specifier|final
name|String
name|familiesArgKey
init|=
literal|"--families="
decl_stmt|;
if|if
condition|(
name|cmd
operator|.
name|startsWith
argument_list|(
name|familiesArgKey
argument_list|)
condition|)
block|{
name|families
operator|=
name|cmd
operator|.
name|substring
argument_list|(
name|familiesArgKey
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|cmd
operator|.
name|startsWith
argument_list|(
literal|"--all.cells"
argument_list|)
condition|)
block|{
name|allCells
operator|=
literal|true
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|i
operator|==
name|args
operator|.
name|length
operator|-
literal|1
condition|)
block|{
name|tableName
operator|=
name|cmd
expr_stmt|;
block|}
else|else
block|{
name|printUsage
argument_list|(
literal|"Invalid argument '"
operator|+
name|cmd
operator|+
literal|"'"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
if|if
condition|(
name|newTableName
operator|==
literal|null
operator|&&
name|peerAddress
operator|==
literal|null
condition|)
block|{
name|printUsage
argument_list|(
literal|"At least a new table name or a "
operator|+
literal|"peer address must be specified"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|(
name|endTime
operator|!=
literal|0
operator|)
operator|&&
operator|(
name|startTime
operator|>
name|endTime
operator|)
condition|)
block|{
name|printUsage
argument_list|(
literal|"Invalid time range filter: starttime="
operator|+
name|startTime
operator|+
literal|">  endtime="
operator|+
name|endTime
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|printUsage
argument_list|(
literal|"Can't start because "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
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
comment|/**    * Main entry point.    *    * @param args  The command line parameters.    * @throws Exception When running the job fails.    */
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
name|int
name|ret
init|=
name|ToolRunner
operator|.
name|run
argument_list|(
operator|new
name|CopyTable
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|)
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|ret
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|run
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|String
index|[]
name|otherArgs
init|=
operator|new
name|GenericOptionsParser
argument_list|(
name|getConf
argument_list|()
argument_list|,
name|args
argument_list|)
operator|.
name|getRemainingArgs
argument_list|()
decl_stmt|;
name|Job
name|job
init|=
name|createSubmittableJob
argument_list|(
name|getConf
argument_list|()
argument_list|,
name|otherArgs
argument_list|)
decl_stmt|;
if|if
condition|(
name|job
operator|==
literal|null
condition|)
return|return
literal|1
return|;
return|return
name|job
operator|.
name|waitForCompletion
argument_list|(
literal|true
argument_list|)
condition|?
literal|0
else|:
literal|1
return|;
block|}
block|}
end_class

end_unit

