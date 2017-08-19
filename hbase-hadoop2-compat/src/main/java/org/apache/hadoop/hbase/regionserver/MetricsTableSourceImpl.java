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
name|regionserver
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
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
name|TableName
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
name|hadoop
operator|.
name|hbase
operator|.
name|metrics
operator|.
name|Interns
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
name|metrics2
operator|.
name|MetricsRecordBuilder
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
name|metrics2
operator|.
name|lib
operator|.
name|DynamicMetricsRegistry
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetricsTableSourceImpl
implements|implements
name|MetricsTableSource
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
name|MetricsTableSourceImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|AtomicBoolean
name|closed
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
comment|// Non-final so that we can null out the wrapper
comment|// This is just paranoia. We really really don't want to
comment|// leak a whole table by way of keeping the
comment|// tableWrapper around too long.
specifier|private
name|MetricsTableWrapperAggregate
name|tableWrapperAgg
decl_stmt|;
specifier|private
specifier|final
name|MetricsTableAggregateSourceImpl
name|agg
decl_stmt|;
specifier|private
specifier|final
name|DynamicMetricsRegistry
name|registry
decl_stmt|;
specifier|private
specifier|final
name|String
name|tableNamePrefix
decl_stmt|;
specifier|private
specifier|final
name|TableName
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|int
name|hashCode
decl_stmt|;
specifier|public
name|MetricsTableSourceImpl
parameter_list|(
name|String
name|tblName
parameter_list|,
name|MetricsTableAggregateSourceImpl
name|aggregate
parameter_list|,
name|MetricsTableWrapperAggregate
name|tblWrapperAgg
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Creating new MetricsTableSourceImpl for table "
argument_list|)
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|tblName
argument_list|)
expr_stmt|;
name|this
operator|.
name|agg
operator|=
name|aggregate
expr_stmt|;
name|agg
operator|.
name|register
argument_list|(
name|tblName
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|tableWrapperAgg
operator|=
name|tblWrapperAgg
expr_stmt|;
name|this
operator|.
name|registry
operator|=
name|agg
operator|.
name|getMetricsRegistry
argument_list|()
expr_stmt|;
name|this
operator|.
name|tableNamePrefix
operator|=
literal|"Namespace_"
operator|+
name|this
operator|.
name|tableName
operator|.
name|getNamespaceAsString
argument_list|()
operator|+
literal|"_table_"
operator|+
name|this
operator|.
name|tableName
operator|.
name|getQualifierAsString
argument_list|()
operator|+
literal|"_metric_"
expr_stmt|;
name|this
operator|.
name|hashCode
operator|=
name|this
operator|.
name|tableName
operator|.
name|hashCode
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|boolean
name|wasClosed
init|=
name|closed
operator|.
name|getAndSet
argument_list|(
literal|true
argument_list|)
decl_stmt|;
comment|// Has someone else already closed this for us?
if|if
condition|(
name|wasClosed
condition|)
block|{
return|return;
block|}
comment|// Before removing the metrics remove this table from the aggregate table bean.
comment|// This should mean that it's unlikely that snapshot and close happen at the same time.
name|agg
operator|.
name|deregister
argument_list|(
name|tableName
operator|.
name|getNameAsString
argument_list|()
argument_list|)
expr_stmt|;
comment|// While it's un-likely that snapshot and close happen at the same time it's still possible.
comment|// So grab the lock to ensure that all calls to snapshot are done before we remove the metrics
synchronized|synchronized
init|(
name|this
init|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Removing table Metrics for table "
argument_list|)
expr_stmt|;
block|}
name|tableWrapperAgg
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|MetricsTableAggregateSource
name|getAggregateSource
parameter_list|()
block|{
return|return
name|agg
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|MetricsTableSource
name|source
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|source
operator|instanceof
name|MetricsTableSourceImpl
operator|)
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|MetricsTableSourceImpl
name|impl
init|=
operator|(
name|MetricsTableSourceImpl
operator|)
name|source
decl_stmt|;
if|if
condition|(
name|impl
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
return|return
name|Long
operator|.
name|compare
argument_list|(
name|hashCode
argument_list|,
name|impl
operator|.
name|hashCode
argument_list|)
return|;
block|}
name|void
name|snapshot
parameter_list|(
name|MetricsRecordBuilder
name|mrb
parameter_list|,
name|boolean
name|ignored
parameter_list|)
block|{
comment|// If there is a close that started be double extra sure
comment|// that we're not getting any locks and not putting data
comment|// into the metrics that should be removed. So early out
comment|// before even getting the lock.
if|if
condition|(
name|closed
operator|.
name|get
argument_list|()
condition|)
block|{
return|return;
block|}
comment|// Grab the read
comment|// This ensures that removes of the metrics
comment|// can't happen while we are putting them back in.
synchronized|synchronized
init|(
name|this
init|)
block|{
comment|// It's possible that a close happened between checking
comment|// the closed variable and getting the lock.
if|if
condition|(
name|closed
operator|.
name|get
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|this
operator|.
name|tableWrapperAgg
operator|!=
literal|null
condition|)
block|{
name|mrb
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|tableNamePrefix
operator|+
name|MetricsTableSource
operator|.
name|READ_REQUEST_COUNT
argument_list|,
name|MetricsTableSource
operator|.
name|READ_REQUEST_COUNT_DESC
argument_list|)
argument_list|,
name|tableWrapperAgg
operator|.
name|getReadRequestsCount
argument_list|(
name|tableName
operator|.
name|getNameAsString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|mrb
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|tableNamePrefix
operator|+
name|MetricsTableSource
operator|.
name|WRITE_REQUEST_COUNT
argument_list|,
name|MetricsTableSource
operator|.
name|WRITE_REQUEST_COUNT_DESC
argument_list|)
argument_list|,
name|tableWrapperAgg
operator|.
name|getWriteRequestsCount
argument_list|(
name|tableName
operator|.
name|getNameAsString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|mrb
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|tableNamePrefix
operator|+
name|MetricsTableSource
operator|.
name|TOTAL_REQUEST_COUNT
argument_list|,
name|MetricsTableSource
operator|.
name|TOTAL_REQUEST_COUNT_DESC
argument_list|)
argument_list|,
name|tableWrapperAgg
operator|.
name|getTotalRequestsCount
argument_list|(
name|tableName
operator|.
name|getNameAsString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|mrb
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|tableNamePrefix
operator|+
name|MetricsTableSource
operator|.
name|MEMSTORE_SIZE
argument_list|,
name|MetricsTableSource
operator|.
name|MEMSTORE_SIZE_DESC
argument_list|)
argument_list|,
name|tableWrapperAgg
operator|.
name|getMemstoresSize
argument_list|(
name|tableName
operator|.
name|getNameAsString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|mrb
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|tableNamePrefix
operator|+
name|MetricsTableSource
operator|.
name|STORE_FILE_SIZE
argument_list|,
name|MetricsTableSource
operator|.
name|STORE_FILE_SIZE_DESC
argument_list|)
argument_list|,
name|tableWrapperAgg
operator|.
name|getStoreFilesSize
argument_list|(
name|tableName
operator|.
name|getNameAsString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|mrb
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|tableNamePrefix
operator|+
name|MetricsTableSource
operator|.
name|TABLE_SIZE
argument_list|,
name|MetricsTableSource
operator|.
name|TABLE_SIZE_DESC
argument_list|)
argument_list|,
name|tableWrapperAgg
operator|.
name|getTableSize
argument_list|(
name|tableName
operator|.
name|getNameAsString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
name|tableName
operator|.
name|getNameAsString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|hashCode
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
return|return
operator|(
name|o
operator|instanceof
name|MetricsTableSourceImpl
operator|&&
name|compareTo
argument_list|(
operator|(
name|MetricsTableSourceImpl
operator|)
name|o
argument_list|)
operator|==
literal|0
operator|)
return|;
block|}
specifier|public
name|MetricsTableWrapperAggregate
name|getTableWrapper
parameter_list|()
block|{
return|return
name|tableWrapperAgg
return|;
block|}
specifier|public
name|String
name|getTableNamePrefix
parameter_list|()
block|{
return|return
name|tableNamePrefix
return|;
block|}
block|}
end_class

end_unit

