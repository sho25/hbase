begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|mapred
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringWriter
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
name|Iterator
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
name|java
operator|.
name|util
operator|.
name|Properties
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|parsers
operator|.
name|DocumentBuilder
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|parsers
operator|.
name|DocumentBuilderFactory
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|transform
operator|.
name|Transformer
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|transform
operator|.
name|TransformerFactory
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|transform
operator|.
name|dom
operator|.
name|DOMSource
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|transform
operator|.
name|stream
operator|.
name|StreamResult
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
name|w3c
operator|.
name|dom
operator|.
name|Document
import|;
end_import

begin_import
import|import
name|org
operator|.
name|w3c
operator|.
name|dom
operator|.
name|Element
import|;
end_import

begin_import
import|import
name|org
operator|.
name|w3c
operator|.
name|dom
operator|.
name|Node
import|;
end_import

begin_import
import|import
name|org
operator|.
name|w3c
operator|.
name|dom
operator|.
name|NodeList
import|;
end_import

begin_import
import|import
name|org
operator|.
name|w3c
operator|.
name|dom
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  * Configuration parameters for building a Lucene index  */
end_comment

begin_class
specifier|public
class|class
name|IndexConfiguration
extends|extends
name|Configuration
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
name|IndexConfiguration
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|String
name|HBASE_COLUMN_NAME
init|=
literal|"hbase.column.name"
decl_stmt|;
specifier|static
specifier|final
name|String
name|HBASE_COLUMN_STORE
init|=
literal|"hbase.column.store"
decl_stmt|;
specifier|static
specifier|final
name|String
name|HBASE_COLUMN_INDEX
init|=
literal|"hbase.column.index"
decl_stmt|;
specifier|static
specifier|final
name|String
name|HBASE_COLUMN_TOKENIZE
init|=
literal|"hbase.column.tokenize"
decl_stmt|;
specifier|static
specifier|final
name|String
name|HBASE_COLUMN_BOOST
init|=
literal|"hbase.column.boost"
decl_stmt|;
specifier|static
specifier|final
name|String
name|HBASE_COLUMN_OMIT_NORMS
init|=
literal|"hbase.column.omit.norms"
decl_stmt|;
specifier|static
specifier|final
name|String
name|HBASE_INDEX_ROWKEY_NAME
init|=
literal|"hbase.index.rowkey.name"
decl_stmt|;
specifier|static
specifier|final
name|String
name|HBASE_INDEX_ANALYZER_NAME
init|=
literal|"hbase.index.analyzer.name"
decl_stmt|;
specifier|static
specifier|final
name|String
name|HBASE_INDEX_MAX_BUFFERED_DOCS
init|=
literal|"hbase.index.max.buffered.docs"
decl_stmt|;
specifier|static
specifier|final
name|String
name|HBASE_INDEX_MAX_BUFFERED_DELS
init|=
literal|"hbase.index.max.buffered.dels"
decl_stmt|;
specifier|static
specifier|final
name|String
name|HBASE_INDEX_MAX_FIELD_LENGTH
init|=
literal|"hbase.index.max.field.length"
decl_stmt|;
specifier|static
specifier|final
name|String
name|HBASE_INDEX_MAX_MERGE_DOCS
init|=
literal|"hbase.index.max.merge.docs"
decl_stmt|;
specifier|static
specifier|final
name|String
name|HBASE_INDEX_MERGE_FACTOR
init|=
literal|"hbase.index.merge.factor"
decl_stmt|;
comment|// double ramBufferSizeMB;
specifier|static
specifier|final
name|String
name|HBASE_INDEX_SIMILARITY_NAME
init|=
literal|"hbase.index.similarity.name"
decl_stmt|;
specifier|static
specifier|final
name|String
name|HBASE_INDEX_USE_COMPOUND_FILE
init|=
literal|"hbase.index.use.compound.file"
decl_stmt|;
specifier|static
specifier|final
name|String
name|HBASE_INDEX_OPTIMIZE
init|=
literal|"hbase.index.optimize"
decl_stmt|;
specifier|public
specifier|static
class|class
name|ColumnConf
extends|extends
name|Properties
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|7419012290580607821L
decl_stmt|;
name|boolean
name|getBoolean
parameter_list|(
name|String
name|name
parameter_list|,
name|boolean
name|defaultValue
parameter_list|)
block|{
name|String
name|valueString
init|=
name|getProperty
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
literal|"true"
operator|.
name|equals
argument_list|(
name|valueString
argument_list|)
condition|)
return|return
literal|true
return|;
elseif|else
if|if
condition|(
literal|"false"
operator|.
name|equals
argument_list|(
name|valueString
argument_list|)
condition|)
return|return
literal|false
return|;
else|else
return|return
name|defaultValue
return|;
block|}
name|void
name|setBoolean
parameter_list|(
name|String
name|name
parameter_list|,
name|boolean
name|value
parameter_list|)
block|{
name|setProperty
argument_list|(
name|name
argument_list|,
name|Boolean
operator|.
name|toString
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|float
name|getFloat
parameter_list|(
name|String
name|name
parameter_list|,
name|float
name|defaultValue
parameter_list|)
block|{
name|String
name|valueString
init|=
name|getProperty
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|valueString
operator|==
literal|null
condition|)
return|return
name|defaultValue
return|;
try|try
block|{
return|return
name|Float
operator|.
name|parseFloat
argument_list|(
name|valueString
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
return|return
name|defaultValue
return|;
block|}
block|}
name|void
name|setFloat
parameter_list|(
name|String
name|name
parameter_list|,
name|float
name|value
parameter_list|)
block|{
name|setProperty
argument_list|(
name|name
argument_list|,
name|Float
operator|.
name|toString
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|ColumnConf
argument_list|>
name|columnMap
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|ColumnConf
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|Iterator
argument_list|<
name|String
argument_list|>
name|columnNameIterator
parameter_list|()
block|{
return|return
name|columnMap
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isIndex
parameter_list|(
name|String
name|columnName
parameter_list|)
block|{
return|return
name|getColumn
argument_list|(
name|columnName
argument_list|)
operator|.
name|getBoolean
argument_list|(
name|HBASE_COLUMN_INDEX
argument_list|,
literal|true
argument_list|)
return|;
block|}
specifier|public
name|void
name|setIndex
parameter_list|(
name|String
name|columnName
parameter_list|,
name|boolean
name|index
parameter_list|)
block|{
name|getColumn
argument_list|(
name|columnName
argument_list|)
operator|.
name|setBoolean
argument_list|(
name|HBASE_COLUMN_INDEX
argument_list|,
name|index
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|isStore
parameter_list|(
name|String
name|columnName
parameter_list|)
block|{
return|return
name|getColumn
argument_list|(
name|columnName
argument_list|)
operator|.
name|getBoolean
argument_list|(
name|HBASE_COLUMN_STORE
argument_list|,
literal|false
argument_list|)
return|;
block|}
specifier|public
name|void
name|setStore
parameter_list|(
name|String
name|columnName
parameter_list|,
name|boolean
name|store
parameter_list|)
block|{
name|getColumn
argument_list|(
name|columnName
argument_list|)
operator|.
name|setBoolean
argument_list|(
name|HBASE_COLUMN_STORE
argument_list|,
name|store
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|isTokenize
parameter_list|(
name|String
name|columnName
parameter_list|)
block|{
return|return
name|getColumn
argument_list|(
name|columnName
argument_list|)
operator|.
name|getBoolean
argument_list|(
name|HBASE_COLUMN_TOKENIZE
argument_list|,
literal|true
argument_list|)
return|;
block|}
specifier|public
name|void
name|setTokenize
parameter_list|(
name|String
name|columnName
parameter_list|,
name|boolean
name|tokenize
parameter_list|)
block|{
name|getColumn
argument_list|(
name|columnName
argument_list|)
operator|.
name|setBoolean
argument_list|(
name|HBASE_COLUMN_TOKENIZE
argument_list|,
name|tokenize
argument_list|)
expr_stmt|;
block|}
specifier|public
name|float
name|getBoost
parameter_list|(
name|String
name|columnName
parameter_list|)
block|{
return|return
name|getColumn
argument_list|(
name|columnName
argument_list|)
operator|.
name|getFloat
argument_list|(
name|HBASE_COLUMN_BOOST
argument_list|,
literal|1.0f
argument_list|)
return|;
block|}
specifier|public
name|void
name|setBoost
parameter_list|(
name|String
name|columnName
parameter_list|,
name|float
name|boost
parameter_list|)
block|{
name|getColumn
argument_list|(
name|columnName
argument_list|)
operator|.
name|setFloat
argument_list|(
name|HBASE_COLUMN_BOOST
argument_list|,
name|boost
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|isOmitNorms
parameter_list|(
name|String
name|columnName
parameter_list|)
block|{
return|return
name|getColumn
argument_list|(
name|columnName
argument_list|)
operator|.
name|getBoolean
argument_list|(
name|HBASE_COLUMN_OMIT_NORMS
argument_list|,
literal|true
argument_list|)
return|;
block|}
specifier|public
name|void
name|setOmitNorms
parameter_list|(
name|String
name|columnName
parameter_list|,
name|boolean
name|omitNorms
parameter_list|)
block|{
name|getColumn
argument_list|(
name|columnName
argument_list|)
operator|.
name|setBoolean
argument_list|(
name|HBASE_COLUMN_OMIT_NORMS
argument_list|,
name|omitNorms
argument_list|)
expr_stmt|;
block|}
specifier|private
name|ColumnConf
name|getColumn
parameter_list|(
name|String
name|columnName
parameter_list|)
block|{
name|ColumnConf
name|column
init|=
name|columnMap
operator|.
name|get
argument_list|(
name|columnName
argument_list|)
decl_stmt|;
if|if
condition|(
name|column
operator|==
literal|null
condition|)
block|{
name|column
operator|=
operator|new
name|ColumnConf
argument_list|()
expr_stmt|;
name|columnMap
operator|.
name|put
argument_list|(
name|columnName
argument_list|,
name|column
argument_list|)
expr_stmt|;
block|}
return|return
name|column
return|;
block|}
specifier|public
name|String
name|getAnalyzerName
parameter_list|()
block|{
return|return
name|get
argument_list|(
name|HBASE_INDEX_ANALYZER_NAME
argument_list|,
literal|"org.apache.lucene.analysis.standard.StandardAnalyzer"
argument_list|)
return|;
block|}
specifier|public
name|void
name|setAnalyzerName
parameter_list|(
name|String
name|analyzerName
parameter_list|)
block|{
name|set
argument_list|(
name|HBASE_INDEX_ANALYZER_NAME
argument_list|,
name|analyzerName
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|getMaxBufferedDeleteTerms
parameter_list|()
block|{
return|return
name|getInt
argument_list|(
name|HBASE_INDEX_MAX_BUFFERED_DELS
argument_list|,
literal|1000
argument_list|)
return|;
block|}
specifier|public
name|void
name|setMaxBufferedDeleteTerms
parameter_list|(
name|int
name|maxBufferedDeleteTerms
parameter_list|)
block|{
name|setInt
argument_list|(
name|HBASE_INDEX_MAX_BUFFERED_DELS
argument_list|,
name|maxBufferedDeleteTerms
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|getMaxBufferedDocs
parameter_list|()
block|{
return|return
name|getInt
argument_list|(
name|HBASE_INDEX_MAX_BUFFERED_DOCS
argument_list|,
literal|10
argument_list|)
return|;
block|}
specifier|public
name|void
name|setMaxBufferedDocs
parameter_list|(
name|int
name|maxBufferedDocs
parameter_list|)
block|{
name|setInt
argument_list|(
name|HBASE_INDEX_MAX_BUFFERED_DOCS
argument_list|,
name|maxBufferedDocs
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|getMaxFieldLength
parameter_list|()
block|{
return|return
name|getInt
argument_list|(
name|HBASE_INDEX_MAX_FIELD_LENGTH
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
return|;
block|}
specifier|public
name|void
name|setMaxFieldLength
parameter_list|(
name|int
name|maxFieldLength
parameter_list|)
block|{
name|setInt
argument_list|(
name|HBASE_INDEX_MAX_FIELD_LENGTH
argument_list|,
name|maxFieldLength
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|getMaxMergeDocs
parameter_list|()
block|{
return|return
name|getInt
argument_list|(
name|HBASE_INDEX_MAX_MERGE_DOCS
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
return|;
block|}
specifier|public
name|void
name|setMaxMergeDocs
parameter_list|(
name|int
name|maxMergeDocs
parameter_list|)
block|{
name|setInt
argument_list|(
name|HBASE_INDEX_MAX_MERGE_DOCS
argument_list|,
name|maxMergeDocs
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|getMergeFactor
parameter_list|()
block|{
return|return
name|getInt
argument_list|(
name|HBASE_INDEX_MERGE_FACTOR
argument_list|,
literal|10
argument_list|)
return|;
block|}
specifier|public
name|void
name|setMergeFactor
parameter_list|(
name|int
name|mergeFactor
parameter_list|)
block|{
name|setInt
argument_list|(
name|HBASE_INDEX_MERGE_FACTOR
argument_list|,
name|mergeFactor
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getRowkeyName
parameter_list|()
block|{
return|return
name|get
argument_list|(
name|HBASE_INDEX_ROWKEY_NAME
argument_list|,
literal|"ROWKEY"
argument_list|)
return|;
block|}
specifier|public
name|void
name|setRowkeyName
parameter_list|(
name|String
name|rowkeyName
parameter_list|)
block|{
name|set
argument_list|(
name|HBASE_INDEX_ROWKEY_NAME
argument_list|,
name|rowkeyName
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getSimilarityName
parameter_list|()
block|{
return|return
name|get
argument_list|(
name|HBASE_INDEX_SIMILARITY_NAME
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|public
name|void
name|setSimilarityName
parameter_list|(
name|String
name|similarityName
parameter_list|)
block|{
name|set
argument_list|(
name|HBASE_INDEX_SIMILARITY_NAME
argument_list|,
name|similarityName
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|isUseCompoundFile
parameter_list|()
block|{
return|return
name|getBoolean
argument_list|(
name|HBASE_INDEX_USE_COMPOUND_FILE
argument_list|,
literal|false
argument_list|)
return|;
block|}
specifier|public
name|void
name|setUseCompoundFile
parameter_list|(
name|boolean
name|useCompoundFile
parameter_list|)
block|{
name|setBoolean
argument_list|(
name|HBASE_INDEX_USE_COMPOUND_FILE
argument_list|,
name|useCompoundFile
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|doOptimize
parameter_list|()
block|{
return|return
name|getBoolean
argument_list|(
name|HBASE_INDEX_OPTIMIZE
argument_list|,
literal|true
argument_list|)
return|;
block|}
specifier|public
name|void
name|setDoOptimize
parameter_list|(
name|boolean
name|doOptimize
parameter_list|)
block|{
name|setBoolean
argument_list|(
name|HBASE_INDEX_OPTIMIZE
argument_list|,
name|doOptimize
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addFromXML
parameter_list|(
name|String
name|content
parameter_list|)
block|{
try|try
block|{
name|DocumentBuilder
name|builder
init|=
name|DocumentBuilderFactory
operator|.
name|newInstance
argument_list|()
operator|.
name|newDocumentBuilder
argument_list|()
decl_stmt|;
name|Document
name|doc
init|=
name|builder
operator|.
name|parse
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|content
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Element
name|root
init|=
name|doc
operator|.
name|getDocumentElement
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
literal|"configuration"
operator|.
name|equals
argument_list|(
name|root
operator|.
name|getTagName
argument_list|()
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|fatal
argument_list|(
literal|"bad conf file: top-level element not<configuration>"
argument_list|)
expr_stmt|;
block|}
name|NodeList
name|props
init|=
name|root
operator|.
name|getChildNodes
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|props
operator|.
name|getLength
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Node
name|propNode
init|=
name|props
operator|.
name|item
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|propNode
operator|instanceof
name|Element
operator|)
condition|)
block|{
continue|continue;
block|}
name|Element
name|prop
init|=
operator|(
name|Element
operator|)
name|propNode
decl_stmt|;
if|if
condition|(
literal|"property"
operator|.
name|equals
argument_list|(
name|prop
operator|.
name|getTagName
argument_list|()
argument_list|)
condition|)
block|{
name|propertyFromXML
argument_list|(
name|prop
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"column"
operator|.
name|equals
argument_list|(
name|prop
operator|.
name|getTagName
argument_list|()
argument_list|)
condition|)
block|{
name|columnConfFromXML
argument_list|(
name|prop
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"bad conf content: element neither<property> nor<column>"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|fatal
argument_list|(
literal|"error parsing conf content: "
operator|+
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|propertyFromXML
parameter_list|(
name|Element
name|prop
parameter_list|,
name|Properties
name|properties
parameter_list|)
block|{
name|NodeList
name|fields
init|=
name|prop
operator|.
name|getChildNodes
argument_list|()
decl_stmt|;
name|String
name|attr
init|=
literal|null
decl_stmt|;
name|String
name|value
init|=
literal|null
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|fields
operator|.
name|getLength
argument_list|()
condition|;
name|j
operator|++
control|)
block|{
name|Node
name|fieldNode
init|=
name|fields
operator|.
name|item
argument_list|(
name|j
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|fieldNode
operator|instanceof
name|Element
operator|)
condition|)
block|{
continue|continue;
block|}
name|Element
name|field
init|=
operator|(
name|Element
operator|)
name|fieldNode
decl_stmt|;
if|if
condition|(
literal|"name"
operator|.
name|equals
argument_list|(
name|field
operator|.
name|getTagName
argument_list|()
argument_list|)
condition|)
block|{
name|attr
operator|=
operator|(
operator|(
name|Text
operator|)
name|field
operator|.
name|getFirstChild
argument_list|()
operator|)
operator|.
name|getData
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
literal|"value"
operator|.
name|equals
argument_list|(
name|field
operator|.
name|getTagName
argument_list|()
argument_list|)
operator|&&
name|field
operator|.
name|hasChildNodes
argument_list|()
condition|)
block|{
name|value
operator|=
operator|(
operator|(
name|Text
operator|)
name|field
operator|.
name|getFirstChild
argument_list|()
operator|)
operator|.
name|getData
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|attr
operator|!=
literal|null
operator|&&
name|value
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|properties
operator|==
literal|null
condition|)
block|{
name|set
argument_list|(
name|attr
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|properties
operator|.
name|setProperty
argument_list|(
name|attr
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|columnConfFromXML
parameter_list|(
name|Element
name|column
parameter_list|)
block|{
name|ColumnConf
name|columnConf
init|=
operator|new
name|ColumnConf
argument_list|()
decl_stmt|;
name|NodeList
name|props
init|=
name|column
operator|.
name|getChildNodes
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|props
operator|.
name|getLength
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Node
name|propNode
init|=
name|props
operator|.
name|item
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|propNode
operator|instanceof
name|Element
operator|)
condition|)
block|{
continue|continue;
block|}
name|Element
name|prop
init|=
operator|(
name|Element
operator|)
name|propNode
decl_stmt|;
if|if
condition|(
literal|"property"
operator|.
name|equals
argument_list|(
name|prop
operator|.
name|getTagName
argument_list|()
argument_list|)
condition|)
block|{
name|propertyFromXML
argument_list|(
name|prop
argument_list|,
name|columnConf
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"bad conf content: element not<property>"
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|columnConf
operator|.
name|getProperty
argument_list|(
name|HBASE_COLUMN_NAME
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|columnMap
operator|.
name|put
argument_list|(
name|columnConf
operator|.
name|getProperty
argument_list|(
name|HBASE_COLUMN_NAME
argument_list|)
argument_list|,
name|columnConf
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"bad column conf: name not specified"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|write
parameter_list|(
name|OutputStream
name|out
parameter_list|)
block|{
try|try
block|{
name|Document
name|doc
init|=
name|writeDocument
argument_list|()
decl_stmt|;
name|DOMSource
name|source
init|=
operator|new
name|DOMSource
argument_list|(
name|doc
argument_list|)
decl_stmt|;
name|StreamResult
name|result
init|=
operator|new
name|StreamResult
argument_list|(
name|out
argument_list|)
decl_stmt|;
name|TransformerFactory
name|transFactory
init|=
name|TransformerFactory
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|Transformer
name|transformer
init|=
name|transFactory
operator|.
name|newTransformer
argument_list|()
decl_stmt|;
name|transformer
operator|.
name|transform
argument_list|(
name|source
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|Document
name|writeDocument
parameter_list|()
block|{
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|iter
init|=
name|iterator
argument_list|()
decl_stmt|;
try|try
block|{
name|Document
name|doc
init|=
name|DocumentBuilderFactory
operator|.
name|newInstance
argument_list|()
operator|.
name|newDocumentBuilder
argument_list|()
operator|.
name|newDocument
argument_list|()
decl_stmt|;
name|Element
name|conf
init|=
name|doc
operator|.
name|createElement
argument_list|(
literal|"configuration"
argument_list|)
decl_stmt|;
name|doc
operator|.
name|appendChild
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|conf
operator|.
name|appendChild
argument_list|(
name|doc
operator|.
name|createTextNode
argument_list|(
literal|"\n"
argument_list|)
argument_list|)
expr_stmt|;
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|entry
operator|=
name|iter
operator|.
name|next
argument_list|()
expr_stmt|;
name|String
name|name
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|String
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|writeProperty
argument_list|(
name|doc
argument_list|,
name|conf
argument_list|,
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
name|Iterator
argument_list|<
name|String
argument_list|>
name|columnIter
init|=
name|columnNameIterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|columnIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|writeColumn
argument_list|(
name|doc
argument_list|,
name|conf
argument_list|,
name|columnIter
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|doc
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|writeProperty
parameter_list|(
name|Document
name|doc
parameter_list|,
name|Element
name|parent
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|Element
name|propNode
init|=
name|doc
operator|.
name|createElement
argument_list|(
literal|"property"
argument_list|)
decl_stmt|;
name|parent
operator|.
name|appendChild
argument_list|(
name|propNode
argument_list|)
expr_stmt|;
name|Element
name|nameNode
init|=
name|doc
operator|.
name|createElement
argument_list|(
literal|"name"
argument_list|)
decl_stmt|;
name|nameNode
operator|.
name|appendChild
argument_list|(
name|doc
operator|.
name|createTextNode
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|propNode
operator|.
name|appendChild
argument_list|(
name|nameNode
argument_list|)
expr_stmt|;
name|Element
name|valueNode
init|=
name|doc
operator|.
name|createElement
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
name|valueNode
operator|.
name|appendChild
argument_list|(
name|doc
operator|.
name|createTextNode
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|propNode
operator|.
name|appendChild
argument_list|(
name|valueNode
argument_list|)
expr_stmt|;
name|parent
operator|.
name|appendChild
argument_list|(
name|doc
operator|.
name|createTextNode
argument_list|(
literal|"\n"
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|writeColumn
parameter_list|(
name|Document
name|doc
parameter_list|,
name|Element
name|parent
parameter_list|,
name|String
name|columnName
parameter_list|)
block|{
name|Element
name|column
init|=
name|doc
operator|.
name|createElement
argument_list|(
literal|"column"
argument_list|)
decl_stmt|;
name|parent
operator|.
name|appendChild
argument_list|(
name|column
argument_list|)
expr_stmt|;
name|column
operator|.
name|appendChild
argument_list|(
name|doc
operator|.
name|createTextNode
argument_list|(
literal|"\n"
argument_list|)
argument_list|)
expr_stmt|;
name|ColumnConf
name|columnConf
init|=
name|getColumn
argument_list|(
name|columnName
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|entry
range|:
name|columnConf
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|getKey
argument_list|()
operator|instanceof
name|String
operator|&&
name|entry
operator|.
name|getValue
argument_list|()
operator|instanceof
name|String
condition|)
block|{
name|writeProperty
argument_list|(
name|doc
argument_list|,
name|column
argument_list|,
operator|(
name|String
operator|)
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
operator|(
name|String
operator|)
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringWriter
name|writer
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
try|try
block|{
name|Document
name|doc
init|=
name|writeDocument
argument_list|()
decl_stmt|;
name|DOMSource
name|source
init|=
operator|new
name|DOMSource
argument_list|(
name|doc
argument_list|)
decl_stmt|;
name|StreamResult
name|result
init|=
operator|new
name|StreamResult
argument_list|(
name|writer
argument_list|)
decl_stmt|;
name|TransformerFactory
name|transFactory
init|=
name|TransformerFactory
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|Transformer
name|transformer
init|=
name|transFactory
operator|.
name|newTransformer
argument_list|()
decl_stmt|;
name|transformer
operator|.
name|transform
argument_list|(
name|source
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|writer
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

