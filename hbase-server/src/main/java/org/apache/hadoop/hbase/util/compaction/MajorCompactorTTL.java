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
operator|.
name|compaction
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Optional
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
name|Executors
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
name|client
operator|.
name|ColumnFamilyDescriptor
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
name|Connection
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
name|ConnectionFactory
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
name|RegionInfo
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
name|TableDescriptor
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
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
name|CommandLineParser
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
name|Option
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

begin_comment
comment|/**  * This tool compacts a table's regions that are beyond it's TTL. It helps to save disk space and  * regions become empty as a result of compaction.  */
end_comment

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
name|MajorCompactorTTL
extends|extends
name|MajorCompactor
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
name|MajorCompactorTTL
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|TableDescriptor
name|htd
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|public
name|MajorCompactorTTL
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|TableDescriptor
name|htd
parameter_list|,
name|int
name|concurrency
parameter_list|,
name|long
name|sleepForMs
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|connection
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|htd
operator|=
name|htd
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|htd
operator|.
name|getTableName
argument_list|()
expr_stmt|;
name|this
operator|.
name|storesToCompact
operator|=
name|Sets
operator|.
name|newHashSet
argument_list|()
expr_stmt|;
comment|// Empty set so all stores will be compacted
name|this
operator|.
name|sleepForMs
operator|=
name|sleepForMs
expr_stmt|;
name|this
operator|.
name|executor
operator|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|concurrency
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterCompactionQueues
operator|=
operator|new
name|ClusterCompactionQueues
argument_list|(
name|concurrency
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|MajorCompactorTTL
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Optional
argument_list|<
name|MajorCompactionRequest
argument_list|>
name|getMajorCompactionRequest
parameter_list|(
name|RegionInfo
name|hri
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|MajorCompactionTTLRequest
operator|.
name|newRequest
argument_list|(
name|connection
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|hri
argument_list|,
name|htd
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Set
argument_list|<
name|String
argument_list|>
name|getStoresRequiringCompaction
parameter_list|(
name|MajorCompactionRequest
name|request
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|(
operator|(
name|MajorCompactionTTLRequest
operator|)
name|request
operator|)
operator|.
name|getStoresRequiringCompaction
argument_list|(
name|htd
argument_list|)
operator|.
name|keySet
argument_list|()
return|;
block|}
specifier|public
name|int
name|compactRegionsTTLOnTable
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|table
parameter_list|,
name|int
name|concurrency
parameter_list|,
name|long
name|sleep
parameter_list|,
name|int
name|numServers
parameter_list|,
name|int
name|numRegions
parameter_list|,
name|boolean
name|dryRun
parameter_list|,
name|boolean
name|skipWait
parameter_list|)
throws|throws
name|Exception
block|{
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|TableDescriptor
name|htd
init|=
name|conn
operator|.
name|getAdmin
argument_list|()
operator|.
name|getDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|doesAnyColFamilyHaveTTL
argument_list|(
name|htd
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"No TTL present for CF of table: "
operator|+
name|tableName
operator|+
literal|", skipping compaction"
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Major compacting table "
operator|+
name|tableName
operator|+
literal|" based on TTL"
argument_list|)
expr_stmt|;
name|MajorCompactor
name|compactor
init|=
operator|new
name|MajorCompactorTTL
argument_list|(
name|conf
argument_list|,
name|htd
argument_list|,
name|concurrency
argument_list|,
name|sleep
argument_list|)
decl_stmt|;
name|compactor
operator|.
name|setNumServers
argument_list|(
name|numServers
argument_list|)
expr_stmt|;
name|compactor
operator|.
name|setNumRegions
argument_list|(
name|numRegions
argument_list|)
expr_stmt|;
name|compactor
operator|.
name|setSkipWait
argument_list|(
name|skipWait
argument_list|)
expr_stmt|;
name|compactor
operator|.
name|initializeWorkQueues
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|dryRun
condition|)
block|{
name|compactor
operator|.
name|compactAllRegions
argument_list|()
expr_stmt|;
block|}
name|compactor
operator|.
name|shutdown
argument_list|()
expr_stmt|;
return|return
name|ERRORS
operator|.
name|size
argument_list|()
return|;
block|}
specifier|private
name|boolean
name|doesAnyColFamilyHaveTTL
parameter_list|(
name|TableDescriptor
name|htd
parameter_list|)
block|{
for|for
control|(
name|ColumnFamilyDescriptor
name|descriptor
range|:
name|htd
operator|.
name|getColumnFamilies
argument_list|()
control|)
block|{
if|if
condition|(
name|descriptor
operator|.
name|getTimeToLive
argument_list|()
operator|!=
name|HConstants
operator|.
name|FOREVER
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|Options
name|getOptions
parameter_list|()
block|{
name|Options
name|options
init|=
name|getCommonOptions
argument_list|()
decl_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|Option
operator|.
name|builder
argument_list|(
literal|"table"
argument_list|)
operator|.
name|required
argument_list|()
operator|.
name|desc
argument_list|(
literal|"table name"
argument_list|)
operator|.
name|hasArg
argument_list|()
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|options
return|;
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
name|Options
name|options
init|=
name|getOptions
argument_list|()
decl_stmt|;
specifier|final
name|CommandLineParser
name|cmdLineParser
init|=
operator|new
name|DefaultParser
argument_list|()
decl_stmt|;
name|CommandLine
name|commandLine
decl_stmt|;
try|try
block|{
name|commandLine
operator|=
name|cmdLineParser
operator|.
name|parse
argument_list|(
name|options
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|parseException
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"ERROR: Unable to parse command-line arguments "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|args
argument_list|)
operator|+
literal|" due to: "
operator|+
name|parseException
argument_list|)
expr_stmt|;
name|printUsage
argument_list|(
name|options
argument_list|)
expr_stmt|;
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|commandLine
operator|==
literal|null
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"ERROR: Failed parse, empty commandLine; "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|printUsage
argument_list|(
name|options
argument_list|)
expr_stmt|;
return|return
operator|-
literal|1
return|;
block|}
name|String
name|table
init|=
name|commandLine
operator|.
name|getOptionValue
argument_list|(
literal|"table"
argument_list|)
decl_stmt|;
name|int
name|numServers
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|commandLine
operator|.
name|getOptionValue
argument_list|(
literal|"numservers"
argument_list|,
literal|"-1"
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|numRegions
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|commandLine
operator|.
name|getOptionValue
argument_list|(
literal|"numregions"
argument_list|,
literal|"-1"
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|concurrency
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|commandLine
operator|.
name|getOptionValue
argument_list|(
literal|"servers"
argument_list|,
literal|"1"
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|sleep
init|=
name|Long
operator|.
name|parseLong
argument_list|(
name|commandLine
operator|.
name|getOptionValue
argument_list|(
literal|"sleep"
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
literal|30000
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|boolean
name|dryRun
init|=
name|commandLine
operator|.
name|hasOption
argument_list|(
literal|"dryRun"
argument_list|)
decl_stmt|;
name|boolean
name|skipWait
init|=
name|commandLine
operator|.
name|hasOption
argument_list|(
literal|"skipWait"
argument_list|)
decl_stmt|;
return|return
name|compactRegionsTTLOnTable
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|,
name|table
argument_list|,
name|concurrency
argument_list|,
name|sleep
argument_list|,
name|numServers
argument_list|,
name|numRegions
argument_list|,
name|dryRun
argument_list|,
name|skipWait
argument_list|)
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
name|MajorCompactorTTL
argument_list|()
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

