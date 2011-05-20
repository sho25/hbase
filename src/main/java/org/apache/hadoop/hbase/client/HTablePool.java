begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|Collection
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
name|PoolMap
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
name|PoolMap
operator|.
name|PoolType
import|;
end_import

begin_comment
comment|/**  * A simple pool of HTable instances.<p>  *  * Each HTablePool acts as a pool for all tables.  To use, instantiate an  * HTablePool and use {@link #getTable(String)} to get an HTable from the pool.  * Once you are done with it, return it to the pool with {@link #putTable(HTableInterface)}.  *   *<p>A pool can be created with a<i>maxSize</i> which defines the most HTable  * references that will ever be retained for each table.  Otherwise the default  * is {@link Integer#MAX_VALUE}.  *  *<p>Pool will manage its own cluster to the cluster. See {@link HConnectionManager}.  */
end_comment

begin_class
specifier|public
class|class
name|HTablePool
implements|implements
name|Closeable
block|{
specifier|private
specifier|final
name|PoolMap
argument_list|<
name|String
argument_list|,
name|HTableInterface
argument_list|>
name|tables
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxSize
decl_stmt|;
specifier|private
specifier|final
name|PoolType
name|poolType
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|config
decl_stmt|;
specifier|private
specifier|final
name|HTableInterfaceFactory
name|tableFactory
decl_stmt|;
comment|/**    * Default Constructor.  Default HBaseConfiguration and no limit on pool size.    */
specifier|public
name|HTablePool
parameter_list|()
block|{
name|this
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor to set maximum versions and use the specified configuration.    * @param config configuration    * @param maxSize maximum number of references to keep for each table    */
specifier|public
name|HTablePool
parameter_list|(
specifier|final
name|Configuration
name|config
parameter_list|,
specifier|final
name|int
name|maxSize
parameter_list|)
block|{
name|this
argument_list|(
name|config
argument_list|,
name|maxSize
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor to set maximum versions and use the specified configuration and    * table factory.    *    * @param config    *          configuration    * @param maxSize    *          maximum number of references to keep for each table    * @param tableFactory    *          table factory    */
specifier|public
name|HTablePool
parameter_list|(
specifier|final
name|Configuration
name|config
parameter_list|,
specifier|final
name|int
name|maxSize
parameter_list|,
specifier|final
name|HTableInterfaceFactory
name|tableFactory
parameter_list|)
block|{
name|this
argument_list|(
name|config
argument_list|,
name|maxSize
argument_list|,
literal|null
argument_list|,
name|PoolType
operator|.
name|Reusable
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor to set maximum versions and use the specified configuration and    * pool type.    *    * @param config    *          configuration    * @param maxSize    *          maximum number of references to keep for each table    * @param tableFactory    *          table factory    * @param poolType    *          pool type which is one of {@link PoolType#Reusable} or    *          {@link PoolType#ThreadLocal}    */
specifier|public
name|HTablePool
parameter_list|(
specifier|final
name|Configuration
name|config
parameter_list|,
specifier|final
name|int
name|maxSize
parameter_list|,
specifier|final
name|PoolType
name|poolType
parameter_list|)
block|{
name|this
argument_list|(
name|config
argument_list|,
name|maxSize
argument_list|,
literal|null
argument_list|,
name|poolType
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor to set maximum versions and use the specified configuration,    * table factory and pool type. The HTablePool supports the    * {@link PoolType#Reusable} and {@link PoolType#ThreadLocal}. If the pool    * type is null or not one of those two values, then it will default to    * {@link PoolType#Reusable}.    *    * @param config    *          configuration    * @param maxSize    *          maximum number of references to keep for each table    * @param tableFactory    *          table factory    * @param poolType    *          pool type which is one of {@link PoolType#Reusable} or    *          {@link PoolType#ThreadLocal}    */
specifier|public
name|HTablePool
parameter_list|(
specifier|final
name|Configuration
name|config
parameter_list|,
specifier|final
name|int
name|maxSize
parameter_list|,
specifier|final
name|HTableInterfaceFactory
name|tableFactory
parameter_list|,
name|PoolType
name|poolType
parameter_list|)
block|{
comment|// Make a new configuration instance so I can safely cleanup when
comment|// done with the pool.
name|this
operator|.
name|config
operator|=
name|config
operator|==
literal|null
condition|?
operator|new
name|Configuration
argument_list|()
else|:
name|config
expr_stmt|;
name|this
operator|.
name|maxSize
operator|=
name|maxSize
expr_stmt|;
name|this
operator|.
name|tableFactory
operator|=
name|tableFactory
operator|==
literal|null
condition|?
operator|new
name|HTableFactory
argument_list|()
else|:
name|tableFactory
expr_stmt|;
if|if
condition|(
name|poolType
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|poolType
operator|=
name|PoolType
operator|.
name|Reusable
expr_stmt|;
block|}
else|else
block|{
switch|switch
condition|(
name|poolType
condition|)
block|{
case|case
name|Reusable
case|:
case|case
name|ThreadLocal
case|:
name|this
operator|.
name|poolType
operator|=
name|poolType
expr_stmt|;
break|break;
default|default:
name|this
operator|.
name|poolType
operator|=
name|PoolType
operator|.
name|Reusable
expr_stmt|;
break|break;
block|}
block|}
name|this
operator|.
name|tables
operator|=
operator|new
name|PoolMap
argument_list|<
name|String
argument_list|,
name|HTableInterface
argument_list|>
argument_list|(
name|this
operator|.
name|poolType
argument_list|,
name|this
operator|.
name|maxSize
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get a reference to the specified table from the pool.<p>    *    * Create a new one if one is not available.    * @param tableName table name    * @return a reference to the specified table    * @throws RuntimeException if there is a problem instantiating the HTable    */
specifier|public
name|HTableInterface
name|getTable
parameter_list|(
name|String
name|tableName
parameter_list|)
block|{
name|HTableInterface
name|table
init|=
name|tables
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|table
operator|==
literal|null
condition|)
block|{
name|table
operator|=
name|createHTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
return|return
name|table
return|;
block|}
comment|/**    * Get a reference to the specified table from the pool.<p>    *    * Create a new one if one is not available.    * @param tableName table name    * @return a reference to the specified table    * @throws RuntimeException if there is a problem instantiating the HTable    */
specifier|public
name|HTableInterface
name|getTable
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
block|{
return|return
name|getTable
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Puts the specified HTable back into the pool.<p>    *    * If the pool already contains<i>maxSize</i> references to the table,    * then the table instance gets closed after flushing buffered edits.    * @param table table    */
specifier|public
name|void
name|putTable
parameter_list|(
name|HTableInterface
name|table
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|tableName
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|tables
operator|.
name|size
argument_list|(
name|tableName
argument_list|)
operator|>=
name|maxSize
condition|)
block|{
comment|// release table instance since we're not reusing it
name|this
operator|.
name|tableFactory
operator|.
name|releaseHTableInterface
argument_list|(
name|table
argument_list|)
expr_stmt|;
return|return;
block|}
name|tables
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
name|table
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|HTableInterface
name|createHTable
parameter_list|(
name|String
name|tableName
parameter_list|)
block|{
return|return
name|this
operator|.
name|tableFactory
operator|.
name|createHTableInterface
argument_list|(
name|config
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Closes all the HTable instances , belonging to the given table, in the table pool.    *<p>    * Note: this is a 'shutdown' of the given table pool and different from    * {@link #putTable(HTableInterface)}, that is used to return the table    * instance to the pool for future re-use.    *    * @param tableName    */
specifier|public
name|void
name|closeTablePool
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|Collection
argument_list|<
name|HTableInterface
argument_list|>
name|tables
init|=
name|this
operator|.
name|tables
operator|.
name|values
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|tables
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|HTableInterface
name|table
range|:
name|tables
control|)
block|{
name|this
operator|.
name|tableFactory
operator|.
name|releaseHTableInterface
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|tables
operator|.
name|remove
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
comment|/**    * See {@link #closeTablePool(String)}.    *    * @param tableName    */
specifier|public
name|void
name|closeTablePool
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|closeTablePool
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Closes all the HTable instances , belonging to all tables in the table pool.    *<p>    * Note: this is a 'shutdown' of all the table pools.    */
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|String
name|tableName
range|:
name|tables
operator|.
name|keySet
argument_list|()
control|)
block|{
name|closeTablePool
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
name|int
name|getCurrentPoolSize
parameter_list|(
name|String
name|tableName
parameter_list|)
block|{
return|return
name|tables
operator|.
name|size
argument_list|(
name|tableName
argument_list|)
return|;
block|}
block|}
end_class

end_unit

