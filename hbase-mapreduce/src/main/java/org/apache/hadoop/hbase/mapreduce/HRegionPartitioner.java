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
name|hadoop
operator|.
name|conf
operator|.
name|Configurable
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
name|RegionLocator
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
name|ImmutableBytesWritable
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
name|mapred
operator|.
name|TableOutputFormat
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
name|Partitioner
import|;
end_import

begin_comment
comment|/**  * This is used to partition the output keys into groups of keys.  * Keys are grouped according to the regions that currently exist  * so that each reducer fills a single region so load is distributed.  *  *<p>This class is not suitable as partitioner creating hfiles  * for incremental bulk loads as region spread will likely change between time of  * hfile creation and load time. See {@link org.apache.hadoop.hbase.tool.BulkLoadHFiles}  * and<a href="http://hbase.apache.org/book.html#arch.bulk.load">Bulk Load</a>.</p>  *  * @param<KEY>  The type of the key.  * @param<VALUE>  The type of the value.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|HRegionPartitioner
parameter_list|<
name|KEY
parameter_list|,
name|VALUE
parameter_list|>
extends|extends
name|Partitioner
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|VALUE
argument_list|>
implements|implements
name|Configurable
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
name|HRegionPartitioner
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
init|=
literal|null
decl_stmt|;
comment|// Connection and locator are not cleaned up; they just die when partitioner is done.
specifier|private
name|Connection
name|connection
decl_stmt|;
specifier|private
name|RegionLocator
name|locator
decl_stmt|;
specifier|private
name|byte
index|[]
index|[]
name|startKeys
decl_stmt|;
comment|/**    * Gets the partition number for a given key (hence record) given the total    * number of partitions i.e. number of reduce-tasks for the job.    *    *<p>Typically a hash function on a all or a subset of the key.</p>    *    * @param key  The key to be partitioned.    * @param value  The entry value.    * @param numPartitions  The total number of partitions.    * @return The partition number for the<code>key</code>.    * @see org.apache.hadoop.mapreduce.Partitioner#getPartition(    *   java.lang.Object, java.lang.Object, int)    */
annotation|@
name|Override
specifier|public
name|int
name|getPartition
parameter_list|(
name|ImmutableBytesWritable
name|key
parameter_list|,
name|VALUE
name|value
parameter_list|,
name|int
name|numPartitions
parameter_list|)
block|{
name|byte
index|[]
name|region
init|=
literal|null
decl_stmt|;
comment|// Only one region return 0
if|if
condition|(
name|this
operator|.
name|startKeys
operator|.
name|length
operator|==
literal|1
condition|)
block|{
return|return
literal|0
return|;
block|}
try|try
block|{
comment|// Not sure if this is cached after a split so we could have problems
comment|// here if a region splits while mapping
name|region
operator|=
name|this
operator|.
name|locator
operator|.
name|getRegionLocation
argument_list|(
name|key
operator|.
name|get
argument_list|()
argument_list|)
operator|.
name|getRegion
argument_list|()
operator|.
name|getStartKey
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|this
operator|.
name|startKeys
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|region
argument_list|,
name|this
operator|.
name|startKeys
index|[
name|i
index|]
argument_list|)
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|i
operator|>=
name|numPartitions
condition|)
block|{
comment|// cover if we have less reduces then regions.
return|return
operator|(
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
operator|.
name|hashCode
argument_list|()
operator|&
name|Integer
operator|.
name|MAX_VALUE
operator|)
operator|%
name|numPartitions
return|;
block|}
return|return
name|i
return|;
block|}
block|}
comment|// if above fails to find start key that match we need to return something
return|return
literal|0
return|;
block|}
comment|/**    * Returns the current configuration.    *    * @return The current configuration.    * @see org.apache.hadoop.conf.Configurable#getConf()    */
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
comment|/**    * Sets the configuration. This is used to determine the start keys for the    * given table.    *    * @param configuration  The configuration to set.    * @see org.apache.hadoop.conf.Configurable#setConf(    *   org.apache.hadoop.conf.Configuration)    */
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|configuration
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|configuration
argument_list|)
expr_stmt|;
try|try
block|{
name|this
operator|.
name|connection
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|TableOutputFormat
operator|.
name|OUTPUT_TABLE
argument_list|)
argument_list|)
decl_stmt|;
name|this
operator|.
name|locator
operator|=
name|this
operator|.
name|connection
operator|.
name|getRegionLocator
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|this
operator|.
name|startKeys
operator|=
name|this
operator|.
name|locator
operator|.
name|getStartKeys
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

