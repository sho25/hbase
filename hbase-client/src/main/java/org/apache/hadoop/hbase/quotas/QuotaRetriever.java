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
name|quotas
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
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
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
name|Result
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
name|ResultScanner
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
name|client
operator|.
name|Table
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
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * Scanner to iterate over the quota settings.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|QuotaRetriever
implements|implements
name|Closeable
implements|,
name|Iterable
argument_list|<
name|QuotaSettings
argument_list|>
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
name|QuotaRetriever
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Queue
argument_list|<
name|QuotaSettings
argument_list|>
name|cache
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|ResultScanner
name|scanner
decl_stmt|;
comment|/**    * Connection to use.    * Could pass one in and have this class use it but this class wants to be standalone.    */
specifier|private
name|Connection
name|connection
decl_stmt|;
specifier|private
name|Table
name|table
decl_stmt|;
comment|/**    * Should QutoaRetriever manage the state of the connection, or leave it be.    */
specifier|private
name|boolean
name|isManagedConnection
init|=
literal|false
decl_stmt|;
name|QuotaRetriever
parameter_list|()
block|{   }
name|void
name|init
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Scan
name|scan
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Set this before creating the connection and passing it down to make sure
comment|// it's cleaned up if we fail to construct the Scanner.
name|this
operator|.
name|isManagedConnection
operator|=
literal|true
expr_stmt|;
name|init
argument_list|(
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
argument_list|,
name|scan
argument_list|)
expr_stmt|;
block|}
name|void
name|init
parameter_list|(
specifier|final
name|Connection
name|conn
parameter_list|,
specifier|final
name|Scan
name|scan
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|connection
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|conn
argument_list|)
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|this
operator|.
name|connection
operator|.
name|getTable
argument_list|(
name|QuotaTableUtil
operator|.
name|QUOTA_TABLE_NAME
argument_list|)
expr_stmt|;
try|try
block|{
name|scanner
operator|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
try|try
block|{
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed getting scanner and then failed close on cleanup"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
throw|throw
name|e
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|table
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|this
operator|.
name|table
operator|=
literal|null
expr_stmt|;
block|}
comment|// Null out the connection on close() even if we didn't explicitly close it
comment|// to maintain typical semantics.
if|if
condition|(
name|isManagedConnection
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|connection
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
name|this
operator|.
name|connection
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|QuotaSettings
name|next
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|cache
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|Result
name|result
init|=
name|scanner
operator|.
name|next
argument_list|()
decl_stmt|;
comment|// Skip exceedThrottleQuota row key because this is not a QuotaSettings
if|if
condition|(
name|result
operator|!=
literal|null
operator|&&
name|Bytes
operator|.
name|equals
argument_list|(
name|result
operator|.
name|getRow
argument_list|()
argument_list|,
name|QuotaTableUtil
operator|.
name|getExceedThrottleQuotaRowKey
argument_list|()
argument_list|)
condition|)
block|{
name|result
operator|=
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|QuotaTableUtil
operator|.
name|parseResultToCollection
argument_list|(
name|result
argument_list|,
name|cache
argument_list|)
expr_stmt|;
block|}
return|return
name|cache
operator|.
name|poll
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|QuotaSettings
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
operator|new
name|Iter
argument_list|()
return|;
block|}
specifier|private
class|class
name|Iter
implements|implements
name|Iterator
argument_list|<
name|QuotaSettings
argument_list|>
block|{
name|QuotaSettings
name|cache
decl_stmt|;
specifier|public
name|Iter
parameter_list|()
block|{
try|try
block|{
name|cache
operator|=
name|QuotaRetriever
operator|.
name|this
operator|.
name|next
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
name|warn
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|cache
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|QuotaSettings
name|next
parameter_list|()
block|{
name|QuotaSettings
name|result
init|=
name|cache
decl_stmt|;
try|try
block|{
name|cache
operator|=
name|QuotaRetriever
operator|.
name|this
operator|.
name|next
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
name|warn
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"remove() not supported"
argument_list|)
throw|;
block|}
block|}
comment|/**    * Open a QuotaRetriever with no filter, all the quota settings will be returned.    * @param conf Configuration object to use.    * @return the QuotaRetriever    * @throws IOException if a remote or network exception occurs    */
specifier|public
specifier|static
name|QuotaRetriever
name|open
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|open
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Open a QuotaRetriever with the specified filter.    * @param conf Configuration object to use.    * @param filter the QuotaFilter    * @return the QuotaRetriever    * @throws IOException if a remote or network exception occurs    */
specifier|public
specifier|static
name|QuotaRetriever
name|open
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|QuotaFilter
name|filter
parameter_list|)
throws|throws
name|IOException
block|{
name|Scan
name|scan
init|=
name|QuotaTableUtil
operator|.
name|makeScan
argument_list|(
name|filter
argument_list|)
decl_stmt|;
name|QuotaRetriever
name|scanner
init|=
operator|new
name|QuotaRetriever
argument_list|()
decl_stmt|;
name|scanner
operator|.
name|init
argument_list|(
name|conf
argument_list|,
name|scan
argument_list|)
expr_stmt|;
return|return
name|scanner
return|;
block|}
block|}
end_class

end_unit

