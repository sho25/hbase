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
name|Queue
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
name|HTable
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
name|protobuf
operator|.
name|generated
operator|.
name|QuotaProtos
operator|.
name|Quotas
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
annotation|@
name|InterfaceStability
operator|.
name|Evolving
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
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
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
argument_list|<
name|QuotaSettings
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|ResultScanner
name|scanner
decl_stmt|;
specifier|private
name|HTable
name|table
decl_stmt|;
specifier|private
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
name|table
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
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
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|table
operator|.
name|close
argument_list|()
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
if|if
condition|(
name|result
operator|==
literal|null
condition|)
return|return
literal|null
return|;
name|QuotaTableUtil
operator|.
name|parseResult
argument_list|(
name|result
argument_list|,
operator|new
name|QuotaTableUtil
operator|.
name|QuotasVisitor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|visitUserQuotas
parameter_list|(
name|String
name|userName
parameter_list|,
name|Quotas
name|quotas
parameter_list|)
block|{
name|cache
operator|.
name|addAll
argument_list|(
name|QuotaSettingsFactory
operator|.
name|fromUserQuotas
argument_list|(
name|userName
argument_list|,
name|quotas
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|visitUserQuotas
parameter_list|(
name|String
name|userName
parameter_list|,
name|TableName
name|table
parameter_list|,
name|Quotas
name|quotas
parameter_list|)
block|{
name|cache
operator|.
name|addAll
argument_list|(
name|QuotaSettingsFactory
operator|.
name|fromUserQuotas
argument_list|(
name|userName
argument_list|,
name|table
argument_list|,
name|quotas
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|visitUserQuotas
parameter_list|(
name|String
name|userName
parameter_list|,
name|String
name|namespace
parameter_list|,
name|Quotas
name|quotas
parameter_list|)
block|{
name|cache
operator|.
name|addAll
argument_list|(
name|QuotaSettingsFactory
operator|.
name|fromUserQuotas
argument_list|(
name|userName
argument_list|,
name|namespace
argument_list|,
name|quotas
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|visitTableQuotas
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|Quotas
name|quotas
parameter_list|)
block|{
name|cache
operator|.
name|addAll
argument_list|(
name|QuotaSettingsFactory
operator|.
name|fromTableQuotas
argument_list|(
name|tableName
argument_list|,
name|quotas
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|visitNamespaceQuotas
parameter_list|(
name|String
name|namespace
parameter_list|,
name|Quotas
name|quotas
parameter_list|)
block|{
name|cache
operator|.
name|addAll
argument_list|(
name|QuotaSettingsFactory
operator|.
name|fromNamespaceQuotas
argument_list|(
name|namespace
argument_list|,
name|quotas
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
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

