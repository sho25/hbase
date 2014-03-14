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
name|HTableDescriptor
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
name|coprocessor
operator|.
name|Batch
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
name|coprocessor
operator|.
name|Batch
operator|.
name|Callback
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
name|filter
operator|.
name|BinaryComparator
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
name|filter
operator|.
name|CompareFilter
operator|.
name|CompareOp
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
name|ipc
operator|.
name|CoprocessorRpcChannel
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Message
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Service
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
import|;
end_import

begin_comment
comment|/**  * A simple pool of HTable instances.  *  * Each HTablePool acts as a pool for all tables. To use, instantiate an  * HTablePool and use {@link #getTable(String)} to get an HTable from the pool.  *    * This method is not needed anymore, clients should call    * HTableInterface.close() rather than returning the tables to the pool    *  * Once you are done with it, close your instance of {@link HTableInterface}  * by calling {@link HTableInterface#close()} rather than returning the tables  * to the pool with (deprecated) {@link #putTable(HTableInterface)}.  *  *<p>  * A pool can be created with a<i>maxSize</i> which defines the most HTable  * references that will ever be retained for each table. Otherwise the default  * is {@link Integer#MAX_VALUE}.  *  *<p>  * Pool will manage its own connections to the cluster. See  * {@link HConnectionManager}.  * @deprecated as of 0.98.1. See {@link HConnection#getTable(String)}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|Deprecated
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
comment|/**    * Default Constructor. Default HBaseConfiguration and no limit on pool size.    */
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
comment|/**    * Constructor to set maximum versions and use the specified configuration.    *    * @param config    *          configuration    * @param maxSize    *          maximum number of references to keep for each table    */
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
name|tableFactory
argument_list|,
name|PoolType
operator|.
name|Reusable
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor to set maximum versions and use the specified configuration and    * pool type.    *    * @param config    *          configuration    * @param maxSize    *          maximum number of references to keep for each table    * @param poolType    *          pool type which is one of {@link PoolType#Reusable} or    *          {@link PoolType#ThreadLocal}    */
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
name|HBaseConfiguration
operator|.
name|create
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
comment|/**    * Get a reference to the specified table from the pool.    *<p>    *<p/>    *    * @param tableName    *          table name    * @return a reference to the specified table    * @throws RuntimeException    *           if there is a problem instantiating the HTable    */
specifier|public
name|HTableInterface
name|getTable
parameter_list|(
name|String
name|tableName
parameter_list|)
block|{
comment|// call the old getTable implementation renamed to findOrCreateTable
name|HTableInterface
name|table
init|=
name|findOrCreateTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
comment|// return a proxy table so when user closes the proxy, the actual table
comment|// will be returned to the pool
return|return
operator|new
name|PooledHTable
argument_list|(
name|table
argument_list|)
return|;
block|}
comment|/**    * Get a reference to the specified table from the pool.    *<p>    *    * Create a new one if one is not available.    *    * @param tableName    *          table name    * @return a reference to the specified table    * @throws RuntimeException    *           if there is a problem instantiating the HTable    */
specifier|private
name|HTableInterface
name|findOrCreateTable
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
comment|/**    * Get a reference to the specified table from the pool.    *<p>    *    * Create a new one if one is not available.    *    * @param tableName    *          table name    * @return a reference to the specified table    * @throws RuntimeException    *           if there is a problem instantiating the HTable    */
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
comment|/**    * This method is not needed anymore, clients should call    * HTableInterface.close() rather than returning the tables to the pool    *    * @param table    *          the proxy table user got from pool    * @deprecated    */
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
comment|// we need to be sure nobody puts a proxy implementation in the pool
comment|// but if the client code is not updated
comment|// and it will continue to call putTable() instead of calling close()
comment|// then we need to return the wrapped table to the pool instead of the
comment|// proxy
comment|// table
if|if
condition|(
name|table
operator|instanceof
name|PooledHTable
condition|)
block|{
name|returnTable
argument_list|(
operator|(
operator|(
name|PooledHTable
operator|)
name|table
operator|)
operator|.
name|getWrappedTable
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// normally this should not happen if clients pass back the same
comment|// table
comment|// object they got from the pool
comment|// but if it happens then it's better to reject it
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"not a pooled table: "
operator|+
name|table
argument_list|)
throw|;
block|}
block|}
comment|/**    * Puts the specified HTable back into the pool.    *<p>    *    * If the pool already contains<i>maxSize</i> references to the table, then    * the table instance gets closed after flushing buffered edits.    *    * @param table    *          table    */
specifier|private
name|void
name|returnTable
parameter_list|(
name|HTableInterface
name|table
parameter_list|)
throws|throws
name|IOException
block|{
comment|// this is the old putTable method renamed and made private
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
name|tables
operator|.
name|removeValue
argument_list|(
name|tableName
argument_list|,
name|table
argument_list|)
expr_stmt|;
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
comment|/**    * Closes all the HTable instances , belonging to the given table, in the    * table pool.    *<p>    * Note: this is a 'shutdown' of the given table pool and different from    * {@link #putTable(HTableInterface)}, that is used to return the table    * instance to the pool for future re-use.    *    * @param tableName    */
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
comment|/**    * Closes all the HTable instances , belonging to all tables in the table    * pool.    *<p>    * Note: this is a 'shutdown' of all the table pools.    */
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
name|this
operator|.
name|tables
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
specifier|public
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
comment|/**    * A proxy class that implements HTableInterface.close method to return the    * wrapped table back to the table pool    *    */
class|class
name|PooledHTable
implements|implements
name|HTableInterface
block|{
specifier|private
name|boolean
name|open
init|=
literal|false
decl_stmt|;
specifier|private
name|HTableInterface
name|table
decl_stmt|;
comment|// actual table implementation
specifier|public
name|PooledHTable
parameter_list|(
name|HTableInterface
name|table
parameter_list|)
block|{
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|open
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getTableName
parameter_list|()
block|{
name|checkState
argument_list|()
expr_stmt|;
return|return
name|table
operator|.
name|getTableName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableName
name|getName
parameter_list|()
block|{
return|return
name|table
operator|.
name|getName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
name|checkState
argument_list|()
expr_stmt|;
return|return
name|table
operator|.
name|getConfiguration
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|HTableDescriptor
name|getTableDescriptor
parameter_list|()
throws|throws
name|IOException
block|{
name|checkState
argument_list|()
expr_stmt|;
return|return
name|table
operator|.
name|getTableDescriptor
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|exists
parameter_list|(
name|Get
name|get
parameter_list|)
throws|throws
name|IOException
block|{
name|checkState
argument_list|()
expr_stmt|;
return|return
name|table
operator|.
name|exists
argument_list|(
name|get
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Boolean
index|[]
name|exists
parameter_list|(
name|List
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|)
throws|throws
name|IOException
block|{
name|checkState
argument_list|()
expr_stmt|;
return|return
name|table
operator|.
name|exists
argument_list|(
name|gets
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|batch
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|actions
parameter_list|,
name|Object
index|[]
name|results
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|checkState
argument_list|()
expr_stmt|;
name|table
operator|.
name|batch
argument_list|(
name|actions
argument_list|,
name|results
argument_list|)
expr_stmt|;
block|}
comment|/**      * {@inheritDoc}      * @deprecated If any exception is thrown by one of the actions, there is no way to      * retrieve the partially executed results. Use {@link #batch(List, Object[])} instead.      */
annotation|@
name|Override
specifier|public
name|Object
index|[]
name|batch
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|actions
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|checkState
argument_list|()
expr_stmt|;
return|return
name|table
operator|.
name|batch
argument_list|(
name|actions
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Result
name|get
parameter_list|(
name|Get
name|get
parameter_list|)
throws|throws
name|IOException
block|{
name|checkState
argument_list|()
expr_stmt|;
return|return
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Result
index|[]
name|get
parameter_list|(
name|List
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|)
throws|throws
name|IOException
block|{
name|checkState
argument_list|()
expr_stmt|;
return|return
name|table
operator|.
name|get
argument_list|(
name|gets
argument_list|)
return|;
block|}
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
name|Result
name|getRowOrBefore
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|)
throws|throws
name|IOException
block|{
name|checkState
argument_list|()
expr_stmt|;
return|return
name|table
operator|.
name|getRowOrBefore
argument_list|(
name|row
argument_list|,
name|family
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ResultScanner
name|getScanner
parameter_list|(
name|Scan
name|scan
parameter_list|)
throws|throws
name|IOException
block|{
name|checkState
argument_list|()
expr_stmt|;
return|return
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ResultScanner
name|getScanner
parameter_list|(
name|byte
index|[]
name|family
parameter_list|)
throws|throws
name|IOException
block|{
name|checkState
argument_list|()
expr_stmt|;
return|return
name|table
operator|.
name|getScanner
argument_list|(
name|family
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ResultScanner
name|getScanner
parameter_list|(
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|)
throws|throws
name|IOException
block|{
name|checkState
argument_list|()
expr_stmt|;
return|return
name|table
operator|.
name|getScanner
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|put
parameter_list|(
name|Put
name|put
parameter_list|)
throws|throws
name|IOException
block|{
name|checkState
argument_list|()
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|put
parameter_list|(
name|List
argument_list|<
name|Put
argument_list|>
name|puts
parameter_list|)
throws|throws
name|IOException
block|{
name|checkState
argument_list|()
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|puts
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|checkAndPut
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|Put
name|put
parameter_list|)
throws|throws
name|IOException
block|{
name|checkState
argument_list|()
expr_stmt|;
return|return
name|table
operator|.
name|checkAndPut
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|value
argument_list|,
name|put
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|checkAndPut
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|CompareOp
name|compareOp
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|Put
name|put
parameter_list|)
throws|throws
name|IOException
block|{
name|checkState
argument_list|()
expr_stmt|;
return|return
name|table
operator|.
name|checkAndPut
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|compareOp
argument_list|,
name|value
argument_list|,
name|put
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|delete
parameter_list|(
name|Delete
name|delete
parameter_list|)
throws|throws
name|IOException
block|{
name|checkState
argument_list|()
expr_stmt|;
name|table
operator|.
name|delete
argument_list|(
name|delete
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|delete
parameter_list|(
name|List
argument_list|<
name|Delete
argument_list|>
name|deletes
parameter_list|)
throws|throws
name|IOException
block|{
name|checkState
argument_list|()
expr_stmt|;
name|table
operator|.
name|delete
argument_list|(
name|deletes
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|checkAndDelete
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|Delete
name|delete
parameter_list|)
throws|throws
name|IOException
block|{
name|checkState
argument_list|()
expr_stmt|;
return|return
name|table
operator|.
name|checkAndDelete
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|value
argument_list|,
name|delete
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|checkAndDelete
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|CompareOp
name|compareOp
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|Delete
name|delete
parameter_list|)
throws|throws
name|IOException
block|{
name|checkState
argument_list|()
expr_stmt|;
return|return
name|table
operator|.
name|checkAndDelete
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|compareOp
argument_list|,
name|value
argument_list|,
name|delete
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Result
name|increment
parameter_list|(
name|Increment
name|increment
parameter_list|)
throws|throws
name|IOException
block|{
name|checkState
argument_list|()
expr_stmt|;
return|return
name|table
operator|.
name|increment
argument_list|(
name|increment
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|incrementColumnValue
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|long
name|amount
parameter_list|)
throws|throws
name|IOException
block|{
name|checkState
argument_list|()
expr_stmt|;
return|return
name|table
operator|.
name|incrementColumnValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|amount
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|incrementColumnValue
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|long
name|amount
parameter_list|,
name|Durability
name|durability
parameter_list|)
throws|throws
name|IOException
block|{
name|checkState
argument_list|()
expr_stmt|;
return|return
name|table
operator|.
name|incrementColumnValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|amount
argument_list|,
name|durability
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAutoFlush
parameter_list|()
block|{
name|checkState
argument_list|()
expr_stmt|;
return|return
name|table
operator|.
name|isAutoFlush
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|flushCommits
parameter_list|()
throws|throws
name|IOException
block|{
name|checkState
argument_list|()
expr_stmt|;
name|table
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
block|}
comment|/**      * Returns the actual table back to the pool      *      * @throws IOException      */
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|checkState
argument_list|()
expr_stmt|;
name|open
operator|=
literal|false
expr_stmt|;
name|returnTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|CoprocessorRpcChannel
name|coprocessorService
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
block|{
name|checkState
argument_list|()
expr_stmt|;
return|return
name|table
operator|.
name|coprocessorService
argument_list|(
name|row
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
extends|extends
name|Service
parameter_list|,
name|R
parameter_list|>
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|R
argument_list|>
name|coprocessorService
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|service
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
name|Batch
operator|.
name|Call
argument_list|<
name|T
argument_list|,
name|R
argument_list|>
name|callable
parameter_list|)
throws|throws
name|ServiceException
throws|,
name|Throwable
block|{
name|checkState
argument_list|()
expr_stmt|;
return|return
name|table
operator|.
name|coprocessorService
argument_list|(
name|service
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|,
name|callable
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
extends|extends
name|Service
parameter_list|,
name|R
parameter_list|>
name|void
name|coprocessorService
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|service
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
name|Batch
operator|.
name|Call
argument_list|<
name|T
argument_list|,
name|R
argument_list|>
name|callable
parameter_list|,
name|Callback
argument_list|<
name|R
argument_list|>
name|callback
parameter_list|)
throws|throws
name|ServiceException
throws|,
name|Throwable
block|{
name|checkState
argument_list|()
expr_stmt|;
name|table
operator|.
name|coprocessorService
argument_list|(
name|service
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|,
name|callable
argument_list|,
name|callback
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"PooledHTable{"
operator|+
literal|", table="
operator|+
name|table
operator|+
literal|'}'
return|;
block|}
comment|/**      * Expose the wrapped HTable to tests in the same package      *      * @return wrapped htable      */
name|HTableInterface
name|getWrappedTable
parameter_list|()
block|{
return|return
name|table
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|R
parameter_list|>
name|void
name|batchCallback
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|actions
parameter_list|,
name|Object
index|[]
name|results
parameter_list|,
name|Callback
argument_list|<
name|R
argument_list|>
name|callback
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|checkState
argument_list|()
expr_stmt|;
name|table
operator|.
name|batchCallback
argument_list|(
name|actions
argument_list|,
name|results
argument_list|,
name|callback
argument_list|)
expr_stmt|;
block|}
comment|/**      * {@inheritDoc}      * @deprecated If any exception is thrown by one of the actions, there is no way to      * retrieve the partially executed results. Use      * {@link #batchCallback(List, Object[], org.apache.hadoop.hbase.client.coprocessor.Batch.Callback)}      * instead.      */
annotation|@
name|Override
specifier|public
parameter_list|<
name|R
parameter_list|>
name|Object
index|[]
name|batchCallback
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|actions
parameter_list|,
name|Callback
argument_list|<
name|R
argument_list|>
name|callback
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|checkState
argument_list|()
expr_stmt|;
return|return
name|table
operator|.
name|batchCallback
argument_list|(
name|actions
argument_list|,
name|callback
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|mutateRow
parameter_list|(
name|RowMutations
name|rm
parameter_list|)
throws|throws
name|IOException
block|{
name|checkState
argument_list|()
expr_stmt|;
name|table
operator|.
name|mutateRow
argument_list|(
name|rm
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Result
name|append
parameter_list|(
name|Append
name|append
parameter_list|)
throws|throws
name|IOException
block|{
name|checkState
argument_list|()
expr_stmt|;
return|return
name|table
operator|.
name|append
argument_list|(
name|append
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setAutoFlush
parameter_list|(
name|boolean
name|autoFlush
parameter_list|)
block|{
name|checkState
argument_list|()
expr_stmt|;
name|table
operator|.
name|setAutoFlush
argument_list|(
name|autoFlush
argument_list|,
name|autoFlush
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setAutoFlush
parameter_list|(
name|boolean
name|autoFlush
parameter_list|,
name|boolean
name|clearBufferOnFail
parameter_list|)
block|{
name|checkState
argument_list|()
expr_stmt|;
name|table
operator|.
name|setAutoFlush
argument_list|(
name|autoFlush
argument_list|,
name|clearBufferOnFail
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setAutoFlushTo
parameter_list|(
name|boolean
name|autoFlush
parameter_list|)
block|{
name|table
operator|.
name|setAutoFlushTo
argument_list|(
name|autoFlush
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getWriteBufferSize
parameter_list|()
block|{
name|checkState
argument_list|()
expr_stmt|;
return|return
name|table
operator|.
name|getWriteBufferSize
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setWriteBufferSize
parameter_list|(
name|long
name|writeBufferSize
parameter_list|)
throws|throws
name|IOException
block|{
name|checkState
argument_list|()
expr_stmt|;
name|table
operator|.
name|setWriteBufferSize
argument_list|(
name|writeBufferSize
argument_list|)
expr_stmt|;
block|}
name|boolean
name|isOpen
parameter_list|()
block|{
return|return
name|open
return|;
block|}
specifier|private
name|void
name|checkState
parameter_list|()
block|{
if|if
condition|(
operator|!
name|isOpen
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Table="
operator|+
operator|new
name|String
argument_list|(
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|+
literal|" already closed"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|long
name|incrementColumnValue
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|long
name|amount
parameter_list|,
name|boolean
name|writeToWAL
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|table
operator|.
name|incrementColumnValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|amount
argument_list|,
name|writeToWAL
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|R
extends|extends
name|Message
parameter_list|>
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|R
argument_list|>
name|batchCoprocessorService
parameter_list|(
name|Descriptors
operator|.
name|MethodDescriptor
name|method
parameter_list|,
name|Message
name|request
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
name|R
name|responsePrototype
parameter_list|)
throws|throws
name|ServiceException
throws|,
name|Throwable
block|{
name|checkState
argument_list|()
expr_stmt|;
return|return
name|table
operator|.
name|batchCoprocessorService
argument_list|(
name|method
argument_list|,
name|request
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|,
name|responsePrototype
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|R
extends|extends
name|Message
parameter_list|>
name|void
name|batchCoprocessorService
parameter_list|(
name|Descriptors
operator|.
name|MethodDescriptor
name|method
parameter_list|,
name|Message
name|request
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
name|R
name|responsePrototype
parameter_list|,
name|Callback
argument_list|<
name|R
argument_list|>
name|callback
parameter_list|)
throws|throws
name|ServiceException
throws|,
name|Throwable
block|{
name|checkState
argument_list|()
expr_stmt|;
name|table
operator|.
name|batchCoprocessorService
argument_list|(
name|method
argument_list|,
name|request
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|,
name|responsePrototype
argument_list|,
name|callback
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

