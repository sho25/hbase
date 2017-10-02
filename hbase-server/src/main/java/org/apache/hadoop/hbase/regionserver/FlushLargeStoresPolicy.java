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
name|HBaseInterfaceAudience
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

begin_comment
comment|/**  * A {@link FlushPolicy} that only flushes store larger a given threshold. If no store is large  * enough, then all stores will be flushed.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|CONFIG
argument_list|)
specifier|public
specifier|abstract
class|class
name|FlushLargeStoresPolicy
extends|extends
name|FlushPolicy
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
name|FlushLargeStoresPolicy
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HREGION_COLUMNFAMILY_FLUSH_SIZE_LOWER_BOUND
init|=
literal|"hbase.hregion.percolumnfamilyflush.size.lower.bound"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HREGION_COLUMNFAMILY_FLUSH_SIZE_LOWER_BOUND_MIN
init|=
literal|"hbase.hregion.percolumnfamilyflush.size.lower.bound.min"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|DEFAULT_HREGION_COLUMNFAMILY_FLUSH_SIZE_LOWER_BOUND_MIN
init|=
literal|1024
operator|*
literal|1024
operator|*
literal|16L
decl_stmt|;
specifier|protected
name|long
name|flushSizeLowerBound
init|=
operator|-
literal|1
decl_stmt|;
specifier|protected
name|long
name|getFlushSizeLowerBound
parameter_list|(
name|HRegion
name|region
parameter_list|)
block|{
name|int
name|familyNumber
init|=
name|region
operator|.
name|getTableDescriptor
argument_list|()
operator|.
name|getColumnFamilyCount
argument_list|()
decl_stmt|;
comment|// For multiple families, lower bound is the "average flush size" by default
comment|// unless setting in configuration is larger.
name|long
name|flushSizeLowerBound
init|=
name|region
operator|.
name|getMemStoreFlushSize
argument_list|()
operator|/
name|familyNumber
decl_stmt|;
name|long
name|minimumLowerBound
init|=
name|getConf
argument_list|()
operator|.
name|getLong
argument_list|(
name|HREGION_COLUMNFAMILY_FLUSH_SIZE_LOWER_BOUND_MIN
argument_list|,
name|DEFAULT_HREGION_COLUMNFAMILY_FLUSH_SIZE_LOWER_BOUND_MIN
argument_list|)
decl_stmt|;
if|if
condition|(
name|minimumLowerBound
operator|>
name|flushSizeLowerBound
condition|)
block|{
name|flushSizeLowerBound
operator|=
name|minimumLowerBound
expr_stmt|;
block|}
comment|// use the setting in table description if any
name|String
name|flushedSizeLowerBoundString
init|=
name|region
operator|.
name|getTableDescriptor
argument_list|()
operator|.
name|getValue
argument_list|(
name|HREGION_COLUMNFAMILY_FLUSH_SIZE_LOWER_BOUND
argument_list|)
decl_stmt|;
if|if
condition|(
name|flushedSizeLowerBoundString
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"No "
operator|+
name|HREGION_COLUMNFAMILY_FLUSH_SIZE_LOWER_BOUND
operator|+
literal|" set in description of table "
operator|+
name|region
operator|.
name|getTableDescriptor
argument_list|()
operator|.
name|getTableName
argument_list|()
operator|+
literal|", use config ("
operator|+
name|flushSizeLowerBound
operator|+
literal|") instead"
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
try|try
block|{
name|flushSizeLowerBound
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|flushedSizeLowerBoundString
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|nfe
parameter_list|)
block|{
comment|// fall back for fault setting
name|LOG
operator|.
name|warn
argument_list|(
literal|"Number format exception when parsing "
operator|+
name|HREGION_COLUMNFAMILY_FLUSH_SIZE_LOWER_BOUND
operator|+
literal|" for table "
operator|+
name|region
operator|.
name|getTableDescriptor
argument_list|()
operator|.
name|getTableName
argument_list|()
operator|+
literal|":"
operator|+
name|flushedSizeLowerBoundString
operator|+
literal|". "
operator|+
name|nfe
operator|+
literal|", use config ("
operator|+
name|flushSizeLowerBound
operator|+
literal|") instead"
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|flushSizeLowerBound
return|;
block|}
specifier|protected
name|boolean
name|shouldFlush
parameter_list|(
name|HStore
name|store
parameter_list|)
block|{
if|if
condition|(
name|store
operator|.
name|getMemStoreSize
argument_list|()
operator|.
name|getDataSize
argument_list|()
operator|>
name|this
operator|.
name|flushSizeLowerBound
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Flush Column Family "
operator|+
name|store
operator|.
name|getColumnFamilyName
argument_list|()
operator|+
literal|" of "
operator|+
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedName
argument_list|()
operator|+
literal|" because memstoreSize="
operator|+
name|store
operator|.
name|getMemStoreSize
argument_list|()
operator|.
name|getDataSize
argument_list|()
operator|+
literal|"> lower bound="
operator|+
name|this
operator|.
name|flushSizeLowerBound
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

