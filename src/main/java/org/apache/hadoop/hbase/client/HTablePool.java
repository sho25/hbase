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
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Queue
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
name|ConcurrentLinkedQueue
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

begin_comment
comment|/**  * A simple pool of HTable instances.<p>  *  * Each HTablePool acts as a pool for all tables.  To use, instantiate an  * HTablePool and use {@link #getTable(String)} to get an HTable from the pool.  * Once you are done with it, return it to the pool with {@link #putTable(HTableInterface)}.  *   *<p>A pool can be created with a<i>maxSize</i> which defines the most HTable  * references that will ever be retained for each table.  Otherwise the default  * is {@link Integer#MAX_VALUE}.  *  *<p>Pool will manage its own cluster to the cluster. See {@link HConnectionManager}.  */
end_comment

begin_class
specifier|public
class|class
name|HTablePool
block|{
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Queue
argument_list|<
name|HTableInterface
argument_list|>
argument_list|>
name|tables
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|Queue
argument_list|<
name|HTableInterface
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|config
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxSize
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
argument_list|)
expr_stmt|;
block|}
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
operator|new
name|Configuration
argument_list|(
name|config
argument_list|)
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
name|Queue
argument_list|<
name|HTableInterface
argument_list|>
name|queue
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
name|queue
operator|==
literal|null
condition|)
block|{
name|queue
operator|=
operator|new
name|ConcurrentLinkedQueue
argument_list|<
name|HTableInterface
argument_list|>
argument_list|()
expr_stmt|;
name|tables
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
name|queue
argument_list|)
expr_stmt|;
return|return
name|createHTable
argument_list|(
name|tableName
argument_list|)
return|;
block|}
name|HTableInterface
name|table
init|=
name|queue
operator|.
name|poll
argument_list|()
decl_stmt|;
if|if
condition|(
name|table
operator|==
literal|null
condition|)
block|{
return|return
name|createHTable
argument_list|(
name|tableName
argument_list|)
return|;
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
block|{
name|Queue
argument_list|<
name|HTableInterface
argument_list|>
name|queue
init|=
name|tables
operator|.
name|get
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|queue
operator|.
name|size
argument_list|()
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
name|queue
operator|.
name|add
argument_list|(
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
block|{
name|Queue
argument_list|<
name|HTableInterface
argument_list|>
name|queue
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
name|queue
operator|!=
literal|null
condition|)
block|{
name|HTableInterface
name|table
init|=
name|queue
operator|.
name|poll
argument_list|()
decl_stmt|;
while|while
condition|(
name|table
operator|!=
literal|null
condition|)
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
name|table
operator|=
name|queue
operator|.
name|poll
argument_list|()
expr_stmt|;
block|}
block|}
name|HConnectionManager
operator|.
name|deleteConnection
argument_list|(
name|this
operator|.
name|config
argument_list|,
literal|true
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
name|int
name|getCurrentPoolSize
parameter_list|(
name|String
name|tableName
parameter_list|)
block|{
name|Queue
argument_list|<
name|HTableInterface
argument_list|>
name|queue
init|=
name|tables
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
return|return
name|queue
operator|.
name|size
argument_list|()
return|;
block|}
block|}
end_class

end_unit

