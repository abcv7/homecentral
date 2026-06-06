ALTER TABLE home_parcel.parcel ADD COLUMN product_name VARCHAR(256);

COMMENT ON COLUMN home_parcel.parcel.product_name IS '商品名称（AI识别）';
