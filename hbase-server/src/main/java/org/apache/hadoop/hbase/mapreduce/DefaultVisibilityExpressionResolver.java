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
name|mapreduce
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|security
operator|.
name|visibility
operator|.
name|VisibilityConstants
operator|.
name|LABELS_TABLE_FAMILY
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|security
operator|.
name|visibility
operator|.
name|VisibilityConstants
operator|.
name|LABELS_TABLE_NAME
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|security
operator|.
name|visibility
operator|.
name|VisibilityConstants
operator|.
name|LABEL_QUALIFIER
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
name|HashMap
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
name|TableNotFoundException
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
name|Tag
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
name|hbase
operator|.
name|security
operator|.
name|visibility
operator|.
name|Authorizations
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
name|security
operator|.
name|visibility
operator|.
name|VisibilityConstants
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
name|security
operator|.
name|visibility
operator|.
name|VisibilityLabelOrdinalProvider
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
name|security
operator|.
name|visibility
operator|.
name|VisibilityUtils
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
comment|/**  * This implementation creates tags by expanding expression using label ordinal. Labels will be  * serialized in sorted order of it's ordinal.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|DefaultVisibilityExpressionResolver
implements|implements
name|VisibilityExpressionResolver
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
name|DefaultVisibilityExpressionResolver
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|labels
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|this
operator|.
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|()
block|{
comment|// Reading all the labels and ordinal.
comment|// This scan should be done by user with global_admin privileges.. Ensure that it works
name|Table
name|labelsTable
init|=
literal|null
decl_stmt|;
name|Connection
name|connection
init|=
literal|null
decl_stmt|;
try|try
block|{
name|connection
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
expr_stmt|;
try|try
block|{
name|labelsTable
operator|=
name|connection
operator|.
name|getTable
argument_list|(
name|LABELS_TABLE_NAME
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TableNotFoundException
name|e
parameter_list|)
block|{
comment|// Just return with out doing any thing. When the VC is not used we wont be having 'labels'
comment|// table in the cluster.
return|return;
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
literal|"Error opening 'labels' table"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
name|VisibilityUtils
operator|.
name|SYSTEM_LABEL
argument_list|)
argument_list|)
expr_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|LABELS_TABLE_FAMILY
argument_list|,
name|LABEL_QUALIFIER
argument_list|)
expr_stmt|;
name|ResultScanner
name|scanner
init|=
literal|null
decl_stmt|;
try|try
block|{
name|scanner
operator|=
name|labelsTable
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
expr_stmt|;
name|Result
name|next
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|(
name|next
operator|=
name|scanner
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|byte
index|[]
name|row
init|=
name|next
operator|.
name|getRow
argument_list|()
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|next
operator|.
name|getValue
argument_list|(
name|LABELS_TABLE_FAMILY
argument_list|,
name|LABEL_QUALIFIER
argument_list|)
decl_stmt|;
name|labels
operator|.
name|put
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|value
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toInt
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
literal|"Error scanning 'labels' table"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|scanner
operator|!=
literal|null
condition|)
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed reading 'labels' tags"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
return|return;
block|}
finally|finally
block|{
if|if
condition|(
name|labelsTable
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|labelsTable
operator|.
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
literal|"Error closing 'labels' table"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|connection
operator|!=
literal|null
condition|)
try|try
block|{
name|connection
operator|.
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
literal|"Failed close of temporary connection"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Tag
argument_list|>
name|createVisibilityExpTags
parameter_list|(
name|String
name|visExpression
parameter_list|)
throws|throws
name|IOException
block|{
name|VisibilityLabelOrdinalProvider
name|provider
init|=
operator|new
name|VisibilityLabelOrdinalProvider
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|getLabelOrdinal
parameter_list|(
name|String
name|label
parameter_list|)
block|{
name|Integer
name|ordinal
init|=
literal|null
decl_stmt|;
name|ordinal
operator|=
name|labels
operator|.
name|get
argument_list|(
name|label
argument_list|)
expr_stmt|;
if|if
condition|(
name|ordinal
operator|!=
literal|null
condition|)
block|{
return|return
name|ordinal
operator|.
name|intValue
argument_list|()
return|;
block|}
return|return
name|VisibilityConstants
operator|.
name|NON_EXIST_LABEL_ORDINAL
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getLabel
parameter_list|(
name|int
name|ordinal
parameter_list|)
block|{
comment|// Unused
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"getLabel should not be used in VisibilityExpressionResolver"
argument_list|)
throw|;
block|}
block|}
decl_stmt|;
return|return
name|VisibilityUtils
operator|.
name|createVisibilityExpTags
argument_list|(
name|visExpression
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
name|provider
argument_list|)
return|;
block|}
block|}
end_class

end_unit

